package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.text.toLowerCase
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.CompanyFragmentDirections
import com.example.zadanie.R
import com.example.zadanie.data.CompanyDataSource
import com.example.zadanie.model.Company
import com.example.zadanie.model.Element
import java.util.*

class ItemAdapter(private val fragment: Fragment, dataset: Company):
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    private var data = dataset
    private var dataIsSorted: Boolean = false

    class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.companyFrame)
        val companyText: TextView = view.findViewById(R.id.company_title)
        val deleteButton: ImageView = view.findViewById(R.id.deleteCompany)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_company, parent, false)

        return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data.elements[position]

        holder.companyText.text = item.tags.name
        holder.companyFrame.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToCompanyDetailFragment(
                item.tags.name,
                item.tags.amenity,
                item.tags.opening_hours,
                item.tags.website,
                item.tags.phone
            )
            fragment.findNavController().navigate(action)
        }

        holder.deleteButton.setOnClickListener{
            data.elements.remove(item)
            setData(data)
        }
    }

    override fun getItemCount() = data.elements.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(items: Company){
        data = items
        notifyDataSetChanged()
    }

    fun sortData(){
        data = if(isSorted()) {
            Company(data.elements.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>)
        } else {
            Company(data.elements.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>)
        }
        setData(data)
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }
}
