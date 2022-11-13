package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CheckInFragmentDirections
import com.example.zadanie.model.Element
import java.lang.Double.MAX_VALUE
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class NearbyCompaniesAdapter(val fragment: Fragment): RecyclerView.Adapter<NearbyCompaniesAdapter.ElementViewHolder>()  {
    private lateinit var companyList: MutableList<Element>
    private lateinit var context: Context
    private var dataIsSorted: Boolean = false

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.frame)
        val companyText: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ElementViewHolder {
        context = parent.context
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]
        holder.companyText.text = item.tags.name
        holder.companyFrame.setOnClickListener {
            val action = CheckInFragmentDirections.actionCheckInFragmentToCheckInDetailFragment(
                item.id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortCompaniesByName() {
        companyList = if(isSorted()) {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>
        } else {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataNearestDescending(lat: Double, lon: Double) {
        companyList = companyList.sortedBy { getDistance(lat, lon, it.lat, it.lon) } as MutableList<Element>
        notifyDataSetChanged()
    }

    private fun getDistance(lat: Double, lon: Double, currLat: Double, currLon: Double): Double {
        return sqrt((lat - currLat).pow(2) + (lon - currLon).pow(2))
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setElements(elements: MutableList<Element>) {
        companyList = filterNoNameCompanies(elements)
        notifyDataSetChanged()
    }

    private fun filterNoNameCompanies(companies: MutableList<Element>): MutableList<Element> {
        return companies.filter { it.tags.name != "" } as MutableList<Element>
    }

    fun getCompanyList(): MutableList<Element> {
        return companyList
    }
}