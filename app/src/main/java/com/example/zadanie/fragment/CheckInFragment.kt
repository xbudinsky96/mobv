package com.example.zadanie.fragment

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.zadanie.adapter.NearbyCompaniesAdapter
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentCheckInBinding
import com.example.zadanie.model.NearbyCompanyViewModel
import com.example.zadanie.model.loggedInUser
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.util.*


class CheckInFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private var _binding: FragmentCheckInBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var companyViewModel: NearbyCompanyViewModel
    private val adapter = NearbyCompaniesAdapter(this)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentCheckInBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[NearbyCompanyViewModel::class.java]

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        getLocation()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
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
                    loggedInUser.lat = location.latitude
                    loggedInUser.lon = location.longitude
                    apiService.fetchNearbyCompanies(location.latitude, location.longitude, requireContext(), companyViewModel)
                    companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
                        if (elements.isNotEmpty()) {
                            adapter.setCompanies(elements)
                            adapter.sortDataNearestDescending(location.latitude, location.longitude)
                            binding.sortAbc.setOnClickListener {
                                adapter.sortAlphabetically()
                            }
                            binding.sortDistance.setOnClickListener {
                                adapter.sortDataByDistance()
                            }
                            binding.sortPeople.setOnClickListener {
                                adapter.sortPeople()
                            }
                        }
                        binding.list.adapter = adapter
                    }
                }
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            ACCESS_FINE_LOCATION
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