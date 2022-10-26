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
import androidx.navigation.fragment.findNavController
import com.example.zadanie.adapter.ItemAdapter
import com.example.zadanie.data.CompanyDataSource
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.model.Company
import com.example.zadanie.model.Element

class CompanyFragment : Fragment(R.layout.fragment_company) {
    private lateinit var binding: FragmentCompanyBinding

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyBinding.inflate(inflater, container, false)
        val dataSource = CompanyDataSource()

        var companies = context?.let { dataSource.getCompanies(it) }!!
        //filter null name companies
        companies = Company(companies.elements.filter { it.tags.name != null } as MutableList<Element>)

        val recyclerView = binding.recyclerView
        val adapter = ItemAdapter(this, companies)
        val sortButton: Button = binding.sortCompanies
        val ownCompany: Button = binding.addCompany

        recyclerView.adapter = adapter

        sortButton.setOnClickListener {
            adapter.sortData()
        }

        ownCompany.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToInputDataFragment()
            findNavController().navigate(action)
        }
        return binding.root
    }

}