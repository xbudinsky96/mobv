package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CheckInFragmentDirections
import com.example.zadanie.model.Element
import com.example.zadanie.model.loggedInUser
import com.example.zadanie.utilities.getDistanceFromLatLon
import java.util.*

class NearbyCompaniesAdapter(val fragment: Fragment): RecyclerView.Adapter<NearbyCompaniesAdapter.ElementViewHolder>()  {
    private lateinit var companyList: MutableList<Element>
    private lateinit var context: Context
    private var isSortedByName: Boolean = false
    private var isSortedByDistance: Boolean = false
    private var isSortedByUsers: Boolean = false

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]
        val currentDistance = if (loggedInUser.lat != null && loggedInUser.lon != null) {
            val distance = getDistanceFromLatLon(
                loggedInUser.lat!!,
                loggedInUser.lon!!,
                item.lat,
                item.lon
            )
            "${distance.second} ${distance.first}"
        } else ""
        holder.companyText.text = item.tags.name + "\n" + currentDistance
        holder.companyFrame.setOnClickListener {
            val action = CheckInFragmentDirections.actionCheckInFragmentToCheckInDetailFragment(
                item.id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortAlphabetically() {
        companyList = if(isSortedByName()) {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Element>
        } else {
            companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataByDistance() {
        if (loggedInUser.lat == null || loggedInUser.lon == null) {
            Toast.makeText(context, "Location service not permitted!", Toast.LENGTH_SHORT).show()
            return
        }

        companyList = if(isSortedByDistance()) {
            companyList.sortedBy {
                getDistanceFromLatLon(
                    loggedInUser.lat!!,
                    loggedInUser.lon!!,
                    it.lat,
                    it.lon
                ).second
            }.reversed().reversed() as MutableList<Element>
        } else {
            companyList.sortedBy {
                getDistanceFromLatLon(
                    loggedInUser.lat!!,
                    loggedInUser.lon!!,
                    it.lat,
                    it.lon
                ).second
            }.reversed() as MutableList<Element>
        }
        notifyDataSetChanged()
    }

    //@SuppressLint("NotifyDataSetChanged")
    //fun sortPeople() {
    //    companyList = if(isSortedByPeople()) {
    //        companyList.sortedBy { }.reversed().reversed() as MutableList<Element>
    //    } else {
    //        companyList.sortedBy { }.reversed() as MutableList<Element>
    //    }
    //    notifyDataSetChanged()
    //}

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataNearestDescending(lat: Double, lon: Double) {
        companyList = companyList.sortedBy { getDistanceFromLatLon(lat, lon, it.lat, it.lon).second } as MutableList<Element>
        notifyDataSetChanged()
    }

    private fun isSortedByName(): Boolean {
        isSortedByName = isSortedByName.not()
        return isSortedByName
    }

    private fun isSortedByDistance(): Boolean {
        isSortedByDistance = isSortedByDistance.not()
        return isSortedByDistance
    }

    private fun isSortedByPeople(): Boolean {
        isSortedByUsers = isSortedByUsers.not()
        return isSortedByUsers
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