package com.example.zadanie

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.vector.PathNode.Close.equals
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentCheckInDetailBinding
import com.example.zadanie.fragment.CheckInFragment
import com.example.zadanie.model.Company
import com.example.zadanie.model.Element
import com.example.zadanie.model.NearbyCompanyViewModel
import com.example.zadanie.model.Tags
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlin.math.abs

class CheckInDetailFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentCheckInDetailBinding? = null
    private val binding get() = _binding!!
    private val service = ApiService()
    private lateinit var companyViewModel: NearbyCompanyViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var nearestCompany: Element
    private val args: CheckInDetailFragmentArgs by navArgs()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckInDetailBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[NearbyCompanyViewModel::class.java]
        val confirmButton = binding.confirm
        val specifyButton = binding.specify

        specifyButton.setOnClickListener {
            val action = CheckInDetailFragmentDirections.actionCheckInDetailFragmentToCheckInFragment()
            findNavController().navigate(action)
        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        setCompanyDetails()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setCompanyDetails() {
        try {
            service.getCompanyByID(requireContext(), args.id, binding)
            val preferences = requireContext().getSharedPreferences("COMPANY", Context.MODE_PRIVATE)
            println(preferences.getString("name", "not acquired"))
        }
        catch (e: Exception) {
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
                    service.fetchNearbyCompanies(location.latitude, location.longitude, requireContext(), companyViewModel)
                    companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
                        if (elements.isEmpty()) {
                            Toast.makeText(requireContext(), "No data has been retrieved!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            nearestCompany = getNearestCompany(elements, location.latitude, location.longitude)
                            binding.confirm.setOnClickListener {
                                service.checkInCompany(0, nearestCompany)
                            }
                            binding.content.text = "  " + nearestCompany.tags.name
                        }
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun getNearestCompany(companyList: MutableList<Element>, lat: Double, lon: Double): Element {
        var nearestCompany = companyList[0]
        var distance = java.lang.Double.MAX_VALUE

        for (company in companyList) {
            if(company != nearestCompany) {
                val currentDistance = abs(company.lat - lat) + abs(company.lon - lon)
                if (distance == java.lang.Double.MAX_VALUE) {
                    distance = currentDistance
                }
                else if (distance > currentDistance) {
                    distance = currentDistance
                    nearestCompany = company
                }
            }
        }
        return nearestCompany
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
        Toast.makeText(
            requireContext(),
            "Permission Granted!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}