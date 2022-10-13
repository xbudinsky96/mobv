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
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.adapter.ItemAdapter
import com.example.zadanie.data.CompanyDataSource
import com.example.zadanie.model.Company
import com.example.zadanie.model.Element

class CompanyFragment : Fragment(R.layout.fragment_company) {
    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company, container, false)
        val dataSource = CompanyDataSource()

        var companies = context?.let { dataSource.getCompanies(it) }!!
        //filter null name companies
        companies = Company(companies.elements.filter { it.tags.name != null } as MutableList<Element>)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        val adapter = ItemAdapter(this, companies)
        val sortButton: Button = view.findViewById(R.id.sortCompanies)
        val ownCompany: Button = view.findViewById(R.id.addCompany)

        recyclerView.adapter = adapter

        sortButton.setOnClickListener {
            adapter.sortData()
        }

        ownCompany.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToInputDataFragment()
            findNavController().navigate(action)
        }
        return view
    }

}