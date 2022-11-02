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
import com.example.zadanie.adapter.ElementAdapter
import com.example.zadanie.data.CompanyDataSource
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.model.CompanyViewModel

class CompanyFragment : Fragment(R.layout.fragment_company) {
    private lateinit var binding: FragmentCompanyBinding
    private lateinit var companyViewModel: CompanyViewModel
    private val dataSource = CompanyDataSource()

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
        val sortButton: Button = binding.sortCompanies
        val ownCompany: Button = binding.addCompany
        val adapter = ElementAdapter(this, companyViewModel)

        dataSource.fetchData(companyViewModel, requireContext())

        companyViewModel.readData.observe(viewLifecycleOwner) { elements ->
            adapter.setElements(elements)
            recyclerView.adapter = adapter

            sortButton.setOnClickListener {
                adapter.sortData()
            }
        }

        pullToRefresh.setOnRefreshListener {
            dataSource.fetchData(companyViewModel, requireContext())
            pullToRefresh.isRefreshing = false
        }

        ownCompany.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToInputDataFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }
}