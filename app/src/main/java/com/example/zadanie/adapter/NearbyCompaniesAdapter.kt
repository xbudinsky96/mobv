package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CheckInFragmentDirections
import com.example.zadanie.model.Element
import java.lang.Double.MAX_VALUE
import java.util.*
import kotlin.math.abs

class NearbyCompaniesAdapter(val fragment: Fragment): RecyclerView.Adapter<NearbyCompaniesAdapter.ElementViewHolder>()  {
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
        disableButton(holder.deleteButton)
        holder.companyFrame.setOnClickListener {
            val action = CheckInFragmentDirections.actionCheckInFragmentToCheckInDetailFragment(
                item.id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = companyList.size

    private fun disableButton(button: ImageView) {
        button.isEnabled = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataAlphabetically(){
        companyList = if(isSorted()) {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>
        } else {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataNearestDescending(lat: Double, lon: Double) {
        println(companyList.sortBy { getDistance(lat, it.lat, lon, it.lon) })
        notifyDataSetChanged()
    }

    private fun getDistance(lat: Double, lon: Double, currLat: Double, currLon: Double): Double {
        return abs(lat - currLat) + abs(lon - currLon)
    }

    fun getNearest(lat: Double, lon: Double): Element {
        var nearestCompany = companyList[0]
        var distance = MAX_VALUE

        for (company in companyList) {
            if(company != nearestCompany) {
                val currentDistance = getDistance(nearestCompany.lat, nearestCompany.lon, company.lat, company.lon)
                if (distance == MAX_VALUE) {
                    distance = currentDistance
                }
                else if (distance > currentDistance) {
                    distance = currentDistance
                    nearestCompany = company
                }
            }
        }
        return nearestCompany
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

    fun getCompanyList(): MutableList<Element> {
        return companyList
    }
}