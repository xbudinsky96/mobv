package com.example.zadanie

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadanie.adapter.ElementAdapter
import com.example.zadanie.data.CompanyDataSource
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.model.Company
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.Element

class CompanyFragment : Fragment(R.layout.fragment_company) {
    private lateinit var binding: FragmentCompanyBinding
    private lateinit var companyViewModel: CompanyViewModel

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        val dataSource = CompanyDataSource()

        var companies = context?.let { dataSource.getCompanies(it) }!!
        //filter null name companies
        companies = Company(companies.elements.filter { it.tags != null } as MutableList<Element>)

        insertDataToDataBase(companies)

        val recyclerView = binding.recyclerView
        val sortButton: Button = binding.sortCompanies
        val ownCompany: Button = binding.addCompany

        val adapter = ElementAdapter(this)
        companyViewModel.readData.observe(viewLifecycleOwner, Observer {
                elements ->
            adapter.setElements(elements)
            recyclerView.adapter = adapter

            sortButton.setOnClickListener {
                adapter.sortData()
            }
        })

        ownCompany.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToInputDataFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }

    private fun insertDataToDataBase(company: Company) {
        company.elements.forEach { element ->
            companyViewModel.addCompany(element)
        }
    }
}