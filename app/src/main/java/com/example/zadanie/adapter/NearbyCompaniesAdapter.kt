package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.api.apiService
import com.example.zadanie.fragment.CheckInFragmentDirections
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.Element
import com.example.zadanie.model.loggedInUser
import com.example.zadanie.utilities.getDistanceFromLatLon
import com.google.android.material.snackbar.Snackbar
import java.util.*

class NearbyCompaniesAdapter(val fragment: Fragment): RecyclerView.Adapter<NearbyCompaniesAdapter.ElementViewHolder>()  {
    private lateinit var companyList: MutableList<Element>
    private var isSortedByName: Boolean = false
    private var isSortedByDistance: Boolean = false
    private var isSortedByUsers: Boolean = false
    private lateinit var companyViewModel: CompanyViewModel

    class ElementViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val companyFrame: LinearLayout = view.findViewById(R.id.frame)
        val companyText: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ElementViewHolder {
        companyViewModel = ViewModelProvider(fragment)[CompanyViewModel::class.java]
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
        val numberOfUsers = getUsers(item)
        val usersLabel = if (numberOfUsers != 1) "users" else "user"
        holder.companyText.text = item.tags.name + " - $numberOfUsers $usersLabel \n$currentDistance"
        holder.companyFrame.setOnClickListener {
            val action = CheckInFragmentDirections.actionCheckInFragmentToCheckInDetailFragment(
                item.id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        try {
            return companyList.size
        }
        catch (e: Exception) {
            return 0
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortAlphabetically() {
        try {
            companyList = if (isSortedByName()) {
                companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }.reversed()
                    .reversed() as MutableList<Element>
            } else {
                companyList.sortedBy { it.tags.name.lowercase(Locale.ROOT) }
                    .reversed() as MutableList<Element>
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
                            it.lat,
                            it.lon
                        )
                    if (distance.first == "km") (distance.second * 1000) else distance.second
                }.reversed().reversed() as MutableList<Element>
            } else {
                companyList.sortedBy {
                    val distance =
                        getDistanceFromLatLon(
                            loggedInUser.lat!!,
                            loggedInUser.lon!!,
                            it.lat,
                            it.lon
                        )
                    if (distance.first == "km") (distance.second * 1000) else distance.second
                }.reversed() as MutableList<Element>
            }
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortPeople() {
        try {
            companyList = if (isSortedByPeople()) {
                companyList.sortedBy { getUsers(it) }.reversed().reversed() as MutableList<Element>
            } else {
                companyList.sortedBy { getUsers(it) }.reversed() as MutableList<Element>
            }
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    private fun getUsers(company: Element): Int {
        apiService.getCompaniesWithMembers(fragment, null)
        val companyWithMembers = companyViewModel.getCompanyById(company.id.toString())
        return if (companyWithMembers != null) companyWithMembers.users.toInt() else 0
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortDataNearestDescending(lat: Double, lon: Double) {
        try {
            companyList = companyList.sortedBy { getDistanceFromLatLon(lat, lon, it.lat, it.lon).second } as MutableList<Element>
        } catch (_: Exception) { }
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
    fun setCompanies(companies: MutableList<Element>) {
        try {
            companyList = filterNoNameCompanies(companies)
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    private fun filterNoNameCompanies(companies: MutableList<Element>): MutableList<Element> {
        return companies.filter { it.tags.name != "" } as MutableList<Element>
    }

    fun getCompanyList(): MutableList<Element> {
        return companyList
    }
}