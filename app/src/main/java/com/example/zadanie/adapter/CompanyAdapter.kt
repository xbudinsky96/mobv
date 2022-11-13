package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.EdgeEffectCompat.getDistance
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CompanyFragmentDirections
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.loggedInUser
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class CompanyAdapter(private val fragment: Fragment): RecyclerView.Adapter<CompanyAdapter.ElementViewHolder>() {
    private lateinit var companyList: MutableList<CompanyWithMembers>
    private var dataIsSortedAlphabetically: Boolean = false
    private var dataIsSortedDistance: Boolean = false
    private var dataIsSortedByPeople: Boolean = false
    private lateinit var context: Context

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.frame)
        val companyText: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        context = parent.context
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]
        val users = if (item.users.toInt() > 1) " users" else " user"
        val currentDistance = "Distance: " +  getDistance(
            loggedInUser.lat,
            item.lat.toDouble(),
            loggedInUser.lon,
            item.lon.toDouble()
        ).toInt() + " m"

        holder.companyText.text = item.bar_name + " - " + item.users + users + "\n" + currentDistance
        holder.companyFrame.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToCompanyDetailFragment(
                item.bar_name,
                item.bar_type,
                item.lat,
                item.lon,
                item.users.toInt(),
                item.bar_id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortAlphabetically() {
        companyList = if(isSortedAlphabetically()) {
            companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<CompanyWithMembers>
        } else {
            companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }.reversed() as MutableList<CompanyWithMembers>
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataNearestDescending() {
        companyList = if(isSortedDistance()) {
            companyList.sortedBy {
                getDistance(
                    loggedInUser.lat,
                    loggedInUser.lon,
                    it.lat.toDouble(),
                    it.lon.toDouble()
                )
            } as MutableList<CompanyWithMembers>
        } else {
            companyList.sortedBy {
                getDistance(
                    loggedInUser.lat,
                    loggedInUser.lon,
                    it.lat.toDouble(),
                    it.lon.toDouble()
                )
            }.reversed() as MutableList<CompanyWithMembers>
        }
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortPeople() {
        companyList = if(isSortedByPeople()) {
            companyList.sortedBy { it.users }.reversed().reversed() as MutableList<CompanyWithMembers>
        } else {
            companyList.sortedBy { it.users }.reversed() as MutableList<CompanyWithMembers>
        }
        notifyDataSetChanged()
    }

    private fun getDistance(lat: Double, lon: Double, currLat: Double, currLon: Double): Double {
        return sqrt((lat - currLat).pow(2) + (lon - currLon).pow(2))
    }

    private fun isSortedAlphabetically(): Boolean {
        dataIsSortedAlphabetically = dataIsSortedAlphabetically.not()
        return dataIsSortedAlphabetically
    }

    private fun isSortedDistance(): Boolean {
        dataIsSortedDistance = dataIsSortedDistance.not()
        return dataIsSortedDistance
    }

    private fun isSortedByPeople(): Boolean {
        dataIsSortedByPeople = dataIsSortedByPeople.not()
        return dataIsSortedByPeople
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setElements(companies: MutableList<CompanyWithMembers>) {
        companyList = companies
        notifyDataSetChanged()
    }
}