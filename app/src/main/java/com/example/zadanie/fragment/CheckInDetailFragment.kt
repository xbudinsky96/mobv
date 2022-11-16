package com.example.zadanie.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.location.LocationRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.data.apiService
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.Element
import com.example.zadanie.model.NearbyCompanyViewModel
import com.example.zadanie.model.loggedInUser
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlin.math.pow
import kotlin.math.sqrt

class CheckInDetailFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCheckInDetailBinding? = null
    val binding get() = _binding!!
    private lateinit var nearbyCompanyViewModel: NearbyCompanyViewModel
    private lateinit var companyViewModel: CompanyViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var nearestCompany: Element
    private val args: CheckInDetailFragmentArgs by navArgs()
    private val SEARCHPREFIX = "https://www.google.com/maps/@"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInDetailBinding.inflate(inflater, container, false)
        nearbyCompanyViewModel = ViewModelProvider(this)[NearbyCompanyViewModel::class.java]
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        apiService.getCompaniesWithMembers(this)

        val specifyButton = binding.specify
        val showOnMap = binding.showonmap
        showOnMap.isEnabled = false

        specifyButton.setOnClickListener {
            val action = CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCheckInFragment()
            findNavController().navigate(action)
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        getCompany()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getCompany() {
        try {
            if (args.id.toInt() != 0) {
                apiService.getCompanyByID(this, null, args.id)
                cancelAnimation()
            }
        } catch (_: Exception) {
            Log.i("noargs", "No argument found")
        }

        requestLocationPermission()
        val animation = binding.animationView
        animation.setOnClickListener {
            animation.playAnimation()
            Toast.makeText(requireContext(), "Finding nearest company", Toast.LENGTH_SHORT).show()
            getLocation()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (hasLocationPermission()) {
            fusedLocationProviderClient.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, object: CancellationToken()  {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(requireContext(), "Couldn't get location", Toast.LENGTH_SHORT).show()
                }
                else {
                    val lat = location.latitude
                    val lon = location.longitude
                    apiService.fetchNearbyCompanies(lat,lon, requireContext(), nearbyCompanyViewModel)
                    nearbyCompanyViewModel.readData.observe(viewLifecycleOwner) { elements ->
                        if (elements.isEmpty()) {
                            Toast.makeText(requireContext(), "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                            pauseAnimation()
                        }
                        else {
                            setCoordinates(lat.toString(), lon.toString())
                            cancelAnimation()
                            nearestCompany = getNearestCompany(elements, lat, lon)
                            setConfirmButton()
                            setDetails(nearestCompany, binding)

                            //val companyWithMembers = companyViewModel.getCompanyById(nearestCompany.id.toString())
                            //val users = if (companyWithMembers != null) "Users checked in: " + companyWithMembers.users else ""
                            //val openingHours = if(nearestCompany.tags.opening_hours != null) "Opening hours:" + "\n\n" + nearestCompany.tags.opening_hours.replace(", ", "\n") else ""
                            //val tel = if(nearestCompany.tags.phone != null && nearestCompany.tags.phone != "") "TEL: " + nearestCompany.tags.phone else ""
                            //val web = if(nearestCompany.tags.website != null && nearestCompany.tags.website != "") "WEB: " + nearestCompany.tags.website else ""
                            //val contact = if(tel != null || web != null) "Contact us: \n" else ""
//
                            //binding.compName.text = nearestCompany.tags.name
                            //binding.compType.text = nearestCompany.tags.amenity.replace("_", " ")
                            //binding.openingHours.text = openingHours
                            //binding.tel.text = tel
                            //binding.web.text = web
                            //binding.tel.text = tel
                            //binding.contactUs.text = contact
                            //binding.users.text = users
                        }
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    fun setDetails(foundCompany: Element, binding: FragmentCheckInDetailBinding) {
        val companyWithMembers = companyViewModel.getCompanyById(foundCompany.id.toString())
        val users = if (companyWithMembers != null) "Users checked in: " + companyWithMembers.users else "Users checked in: 0"
        val openingHours = if(foundCompany.tags.opening_hours != null && foundCompany.tags.opening_hours != "") "Opening hours:" + "\n\n" + foundCompany.tags.opening_hours.replace(", ", "\n") else "Opening hours not provided"
        val tel = if(foundCompany.tags.phone != null && foundCompany.tags.phone != "") "TEL: " + foundCompany.tags.phone else "TEL: Not provided"
        val web = if(foundCompany.tags.website != null && foundCompany.tags.website != "") "WEB: " + foundCompany.tags.website else "WEB: Not provided"
        val contact = if(tel != null && tel != "" || web != null && web != "") "Contact us: \n" else "Contact not provided"

        binding.compName.text = foundCompany.tags.name
        binding.compType.text = foundCompany.tags.amenity.replace("_", " ")
        binding.openingHours.text = openingHours
        binding.tel.text = tel
        binding.web.text = web
        binding.tel.text = tel
        binding.contactUs.text = contact
        binding.users.text = users
    }

    private fun setCoordinates(latitude: String, longitude: String) {
        val showOnMap = binding.showonmap
        showOnMap.isEnabled = true
        showOnMap.setOnClickListener {
            val queryUrl: Uri = Uri.parse("${SEARCHPREFIX}${latitude},${longitude},16z")
            val show = Intent(Intent.ACTION_VIEW, queryUrl)
            startActivity(show)
        }
    }

    private fun setConfirmButton() {
        val confirmButton = binding.confirm
        confirmButton.setOnClickListener {
            apiService.checkInCompany(nearestCompany, null, this)
        }
        confirmButton.isEnabled = true
    }

    private fun cancelAnimation() {
        val animation = binding.animationView
        animation.cancelAnimation()
        animation.isVisible = false
    }

    private fun pauseAnimation() {
        val animation = binding.animationView
        animation.pauseAnimation()
        animation.isVisible = true
    }

    private fun getNearestCompany(companyList: MutableList<Element>, lat: Double, lon: Double): Element {
        val nearestCompany = companyList.sortedBy { getDistance(lat, lon, it.lat, it.lon) } as MutableList<Element>
        return nearestCompany[0]
    }

    private fun getDistance(lat: Double, lon: Double, currLat: Double, currLon: Double): Double {
        return sqrt((lat - currLat).pow(2) + (lon - currLon).pow(2))
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            CheckInFragment.PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionDenied(this, perms.first())) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("inflater.inflate(R.menu.menuicons, menu)", "com.example.zadanie.R")
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuicons, menu)
        menu.findItem(R.id.check_in).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToLoginFragment())
            apiService.logoutUser(this)
            true
        }
        R.id.manage_friends -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToAddFriendFragment())
            true
        }
        R.id.my_company -> {
            try {
                findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToHomeFragment(
                    loggedInUser.companyId?.toLong()!!))
            }
            catch (e: Exception) {
                Toast.makeText(requireContext(), "You are not checked in!", Toast.LENGTH_SHORT).show()
            }
            true
        }
        R.id.companies_with_members -> {
            findNavController().navigate(CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCompanyFragment())
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }

}