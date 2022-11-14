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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.model.Element
import com.example.zadanie.model.NearbyCompanyViewModel
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
    private val service = ApiService()
    private lateinit var companyViewModel: NearbyCompanyViewModel
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
        companyViewModel = ViewModelProvider(this)[NearbyCompanyViewModel::class.java]
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
                service.getCompanyByID(this, null, args.id)
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
                    service.fetchNearbyCompanies(lat,lon, requireContext(), companyViewModel)
                    companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
                        if (elements.isEmpty()) {
                            Toast.makeText(requireContext(), "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                            pauseAnimation()
                        }
                        else {
                            setCoordinates(lat.toString(), lon.toString())
                            cancelAnimation()
                            nearestCompany = getNearestCompany(elements, lat, lon)
                            setConfirmButton()

                            val openingHours = if(nearestCompany.tags.opening_hours != null) "Opening hours:" + "\n\n" + nearestCompany.tags.opening_hours.replace(", ", "\n") else ""
                            val tel = if(nearestCompany.tags.phone != null && nearestCompany.tags.phone != "") "TEL: " + nearestCompany.tags.phone else ""
                            val web = if(nearestCompany.tags.website != null && nearestCompany.tags.website != "") "WEB: " + nearestCompany.tags.website else ""
                            val contact = if(tel != null || web != null) "Contact us: \n" else ""

                            binding.compName.text = nearestCompany.tags.name
                            binding.compType.text = nearestCompany.tags.amenity.replace("_", " ")
                            binding.openingHours.text = openingHours
                            binding.tel.text = tel
                            binding.web.text = web
                            binding.tel.text = tel
                            binding.contactUs.text = contact
                        }
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
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
            service.checkInCompany(nearestCompany, null, this)
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

}