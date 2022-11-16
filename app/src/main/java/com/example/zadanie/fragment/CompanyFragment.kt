package com.example.zadanie.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.zadanie.R
import com.example.zadanie.adapter.CompanyAdapter
import com.example.zadanie.data.apiService
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.loggedInUser
import com.example.zadanie.ui.login.LoginFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class CompanyFragment : Fragment(R.layout.fragment_company), EasyPermissions.PermissionCallbacks {
    private lateinit var binding: FragmentCompanyBinding
    private lateinit var companyViewModel: CompanyViewModel
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val pullToRefresh: SwipeRefreshLayout = binding.refreshLayout
        val recyclerView = binding.recyclerView
        val sortButtonAlphabetic: Button = binding.sortAbc
        val sortDistance = binding.sortDistance
        val sortPeople = binding.sortPeople
        val adapter = CompanyAdapter(this)

        fetchDataFromAPI(pullToRefresh)
        getLocation()

        companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
            adapter.setElements(elements)
            recyclerView.adapter = adapter

            sortButtonAlphabetic.setOnClickListener {
                adapter.sortAlphabetically()
            }

            sortDistance.setOnClickListener {
                adapter.sortDataByDistance()
            }

            sortPeople.setOnClickListener {
                adapter.sortPeople()
            }
        }

        pullToRefresh.setOnRefreshListener {
            fetchDataFromAPI(pullToRefresh)
        }

        return binding.root
    }

    private fun fetchDataFromAPI(pullToRefresh: SwipeRefreshLayout) {
        pullToRefresh.isRefreshing = true
        apiService.getCompaniesWithMembers(this)
        pullToRefresh.isRefreshing = false
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
        menu.findItem(R.id.companies_with_members).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(CompanyFragmentDirections.actionCompanyFragmentToLoginFragment())
            apiService.logoutUser(this)
            true
        }
        R.id.manage_friends -> {
            findNavController().navigate(CompanyFragmentDirections.actionCompanyFragmentToAddFriendFragment())
            true
        }
        R.id.my_company -> {
            try {
                findNavController().navigate(CompanyFragmentDirections.actionCompanyFragmentToHomeFragment(loggedInUser.companyId?.toLong()!!))
            }
            catch (e: Exception) {
                findNavController().navigate(CompanyFragmentDirections.actionCompanyFragmentToCheckInDetailFragment(0))
            }
            true
        }
        R.id.check_in -> {
            findNavController().navigate(CompanyFragmentDirections.actionCompanyFragmentToCheckInDetailFragment(0))
            true
        }
        else -> { super.onOptionsItemSelected(item) }
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
                    loggedInUser.lon = location.longitude
                    loggedInUser.lat = location.latitude
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
            LoginFragment.PERMISSION_LOCATION_REQUEST_CODE,
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