package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.CompanyFragmentDirections
import com.example.zadanie.model.CompanyWithMembers
import com.example.zadanie.model.loggedInUser
import com.example.zadanie.utilities.getDistanceFromLatLon
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.math.*

class CompanyAdapter(private val fragment: Fragment): RecyclerView.Adapter<CompanyAdapter.ElementViewHolder>() {
    private lateinit var companyList: MutableList<CompanyWithMembers>
    private var isSortedByName: Boolean = false
    private var isSortedByDistance: Boolean = false
    private var isSortedByUsers: Boolean = false

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.frame)
        val companyText: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ElementViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list, parent, false)

        return ElementViewHolder(adapterLayout)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: ElementViewHolder, position: Int) {
        val item = companyList[position]
        val users = if (item.users.toInt() != 1) " users" else " user"

        val currentDistance = if (loggedInUser.lat != null && loggedInUser.lon != null) {
            val distance = getDistanceFromLatLon(
                loggedInUser.lat!!,
                loggedInUser.lon!!,
                item.lat.toDouble(),
                item.lon.toDouble()
            )
            "${distance.second} ${distance.first}"
        } else ""

        holder.companyText.text = item.bar_name + " - " + item.users + users + "\n" + currentDistance
        holder.companyFrame.setOnClickListener {
            val action = CompanyFragmentDirections.actionCompanyFragmentToCheckInDetailFragment(
                item.bar_id.toLong()
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = companyList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortAlphabetically() {
        try {
            companyList = if (isSortedByName()) {
                companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }.reversed()
                    .reversed() as MutableList<CompanyWithMembers>
            } else {
                companyList.sortedBy { it.bar_name.lowercase(Locale.ROOT) }
                    .reversed() as MutableList<CompanyWithMembers>
            }
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataByDistance() {
        try {
            if (loggedInUser.lat == null || loggedInUser.lon == null) {
                Snackbar.make(fragment.requireView(), "Location service not permitted!", Snackbar.LENGTH_SHORT)
                    .show()
                return
            }
            companyList = if (isSortedByDistance()) {
                companyList.sortedBy {
                    val distance =
                        getDistanceFromLatLon(
                            loggedInUser.lat!!,
                            loggedInUser.lon!!,
                            it.lat.toDouble(),
                            it.lon.toDouble()
                        )
                    if (distance.first == "km") (distance.second * 1000) else distance.second
                }.reversed().reversed() as MutableList<CompanyWithMembers>
            } else {
                companyList.sortedBy {
                    val distance =
                        getDistanceFromLatLon(
                            loggedInUser.lat!!,
                            loggedInUser.lon!!,
                            it.lat.toDouble(),
                            it.lon.toDouble()
                        )
                    if (distance.first == "km") (distance.second * 1000) else distance.second
                }.reversed() as MutableList<CompanyWithMembers>
            }
            notifyDataSetChanged()
        }
        catch (_: Exception) { }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortPeople() {
        try {
            companyList = if (isSortedByPeople()) {
                companyList.sortedBy { it.users }.reversed()
                    .reversed() as MutableList<CompanyWithMembers>
            } else {
                companyList.sortedBy { it.users }.reversed() as MutableList<CompanyWithMembers>
            }
            notifyDataSetChanged()
        }
        catch (_: Exception) { }
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
    fun setElements(companies: MutableList<CompanyWithMembers>) {
        companyList = companies
        notifyDataSetChanged()
    }
}