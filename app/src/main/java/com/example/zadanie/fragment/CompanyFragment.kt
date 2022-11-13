package com.example.zadanie.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.zadanie.R
import com.example.zadanie.adapter.CompanyAdapter
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.model.CompanyViewModel

class CompanyFragment : Fragment(R.layout.fragment_company) {
    private lateinit var binding: FragmentCompanyBinding
    private lateinit var companyViewModel: CompanyViewModel
    private val service = ApiService()

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        val pullToRefresh: SwipeRefreshLayout = binding.refreshLayout
        val recyclerView = binding.recyclerView
        val sortButtonAlphabetic: Button = binding.sortAbc
        val sortDistance = binding.sortDistance
        val sortPeople = binding.sortPeople
        val ownCompany: Button = binding.addCompany
        val adapter = CompanyAdapter(this)

        companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
            adapter.setElements(elements)
            recyclerView.adapter = adapter

            sortButtonAlphabetic.setOnClickListener {
                adapter.sortAlphabetically()
            }

            sortDistance.setOnClickListener {
                adapter.sortDataNearestDescending()
            }

            sortPeople.setOnClickListener {
                adapter.sortPeople()
            }
        }

        pullToRefresh.setOnRefreshListener {
            fetchDataFromAPI()
            pullToRefresh.isRefreshing = false
        }

        ownCompany.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToInputDataFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun fetchDataFromAPI() {
        service.getCompaniesWithMembers(requireContext(), companyViewModel)
    }
}