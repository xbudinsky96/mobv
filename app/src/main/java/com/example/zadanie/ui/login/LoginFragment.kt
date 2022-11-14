package com.example.zadanie.ui.login

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadanie.databinding.FragmentLoginBinding
import com.example.zadanie.data.ApiService
import com.example.zadanie.model.UsersViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class LoginFragment : Fragment(), EasyPermissions.PermissionCallbacks {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var usersViewModel: UsersViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    var location: Location? = null

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val userName = binding.username.text
        val password = binding.passwordLogin.text
        val loginButton = binding.register2Button
        val registerButton = binding.registerButton
        val apiService = ApiService()
        usersViewModel = ViewModelProvider(this)[UsersViewModel::class.java]
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        getLocation()

        registerButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()
            findNavController().navigate(action)
        }

        loginButton.setOnClickListener {
            if(userName.isNotEmpty() && password.isNotEmpty()) {
                apiService.loginUser(userName.toString(), password.toString(), this, usersViewModel)
            }
            else {
                Toast.makeText(context, "Enter a username and a password!", Toast.LENGTH_SHORT).show()
            }
        }

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
                    this.location = location
                }
            }
        } else {
            requestLocationPermission()
        }
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
            PERMISSION_LOCATION_REQUEST_CODE,
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            requireContext(),
            "Permission Granted!",
            Toast.LENGTH_SHORT
        ).show()
        getLocation()
    }
}