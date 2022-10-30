package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.CompanyFragmentDirections
import com.example.zadanie.R
import com.example.zadanie.model.Element
import java.util.*

class ElementAdapter(private val fragment: Fragment): RecyclerView.Adapter<ElementAdapter.ElementViewHolder>() {
    private lateinit var elementList: MutableList<Element>
    private var dataIsSorted: Boolean = false

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.companyFrame)
        val companyText: TextView = view.findViewById(R.id.company_title)
        val deleteButton: ImageView = view.findViewById(R.id.deleteCompany)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_company, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = elementList[position]

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
            elementList.remove(item)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = elementList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortData(){
        elementList = if(isSorted()) {
            elementList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>
        } else {
            elementList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setElements(elements: MutableList<Element>) {
        elementList = elements
        notifyDataSetChanged()
    }
}