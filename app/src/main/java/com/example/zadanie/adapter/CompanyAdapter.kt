package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CompanyFragmentDirections
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.Element
import java.util.*

class CompanyAdapter(private val fragment: Fragment, private val companyViewModel: CompanyViewModel): RecyclerView.Adapter<CompanyAdapter.ElementViewHolder>() {
    private lateinit var companyList: MutableList<CompanyWithMembers>
    private var dataIsSorted: Boolean = false
    private lateinit var context: Context

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.companyFrame)
        val companyText: TextView = view.findViewById(R.id.company_title)
        val deleteButton: ImageView = view.findViewById(R.id.deleteCompany)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        context = parent.context
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_company, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]

        holder.companyText.text = item.bar_name
        holder.companyFrame.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToCompanyDetailFragment(
                item.bar_name,
                item.bar_type,
                item.lat,
                item.lon,
                item.users.toInt()
            )
            fragment.findNavController().navigate(action)
        }

        holder.deleteButton.setOnClickListener{
            companyList.remove(item)
            companyViewModel.deleteCompany(item)
            Toast.makeText(context, "Company deleted!", Toast.LENGTH_SHORT).show()
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortData(){
        companyList = if(isSorted()) {
            companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<CompanyWithMembers>
        } else {
            companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }.reversed() as MutableList<CompanyWithMembers>
        }
        notifyDataSetChanged()
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setElements(companies: MutableList<CompanyWithMembers>) {
        companyList = companies
        notifyDataSetChanged()
    }
}