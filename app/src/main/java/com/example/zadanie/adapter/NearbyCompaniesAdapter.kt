package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.model.Element
import java.util.*

class NearbyCompaniesAdapter(): RecyclerView.Adapter<NearbyCompaniesAdapter.ElementViewHolder>()  {
    private lateinit var companyList: MutableList<Element>
    private lateinit var context: Context
    private var dataIsSorted: Boolean = false

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.companyFrame)
        val companyText: TextView = view.findViewById(R.id.company_title)
        val deleteButton: ImageView = view.findViewById(R.id.deleteCompany)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ElementViewHolder {
        context = parent.context
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_company, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]
        holder.companyText.text = item.tags.name
        holder.companyFrame.setOnClickListener {

        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortData(){
        companyList = if(isSorted()) {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>
        } else {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setElements(elements: MutableList<Element>) {
        companyList = elements
        notifyDataSetChanged()
    }
}