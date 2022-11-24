package com.example.zadanie.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentHomeBinding
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.loggedInUser
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!
    private val args: HomeFragmentArgs by navArgs()
    private lateinit var companyViewModel: CompanyViewModel
    private val SEARCHPREFIX = "https://www.google.com/maps/@"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (args.companyId == 0L) {
            Snackbar.make(requireView(), "You are not checked in yet.", Snackbar.LENGTH_SHORT).show()
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCheckInDetailFragment(args.companyId))
        }
        else {
            setData()
        }
    }

    private fun setData() {
        val company = companyViewModel.getCompanyById(args.companyId.toString())
        if (company != null) {
            setDetails(company)
        } else {
            apiService.getCompanyByID(this, args.companyId)
        }

        binding.checkOut.setOnClickListener {
            apiService.checkOutCompany(this)
        }
        binding.showDetails.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCheckInDetailFragment(args.companyId))
        }
    }

    private fun setDetails(company: CompanyWithMembers) {
        binding.companyName.text = company.bar_name
        binding.nameTitle.text = loggedInUser.name
        binding.showOnMap.isEnabled = true
        binding.showOnMap.setOnClickListener {
            val queryUrl: Uri = Uri.parse("$SEARCHPREFIX${company.lat},${company.lon},16z")
            val showOnMap = Intent(Intent.ACTION_VIEW, queryUrl)
            startActivity(showOnMap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("inflater.inflate(R.menu.menuicons, menu)", "com.example.zadanie.R")
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuicons, menu)
        menu.findItem(R.id.my_company).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            apiService.logoutUser(this)
            true
        }
        R.id.manage_friends -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFriendFragment())
            true
        }
        R.id.check_in -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCheckInDetailFragment(0))
            true
        }
        R.id.companies_with_members -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToCompanyFragment())
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }
}