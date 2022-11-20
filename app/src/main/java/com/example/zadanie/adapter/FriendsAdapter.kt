package com.example.zadanie.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.R
import com.example.zadanie.fragment.FriendListFragmentDirections
import com.example.zadanie.model.Friend
import java.util.*

class FriendsAdapter(private val fragment: Fragment): RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {
    private lateinit var friendList: MutableList<Friend>
    private var sortedByName: Boolean = false
    private var sortedByCompany: Boolean = false
    private lateinit var context: Context

    class FriendViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val frame: LinearLayout = view.findViewById(R.id.frame)
        val friendName: TextView = view.findViewById(R.id.name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        context = parent.context
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.list, parent, false)

        return FriendViewHolder(adapterLayout)
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = friendList[position]
        val company = if (item.bar_name != null) "\n\nChecked in: " + item.bar_name else "\n\nNot checked in"

        holder.friendName.text = item.user_name + company
        holder.frame.setOnClickListener {
            try {
                val action = FriendListFragmentDirections.actionFriendListFragmentToCheckInDetailFragment(
                    item.bar_id.toLong(),
                )
                fragment.findNavController().navigate(action)
            }
            catch (_: Exception) { }
        }
    }

    override fun getItemCount() = friendList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortFriendsByName(){
        try {
            friendList = if (isSortedByName()) {
                friendList.sortedBy { it.user_name.lowercase(Locale.ROOT) }.reversed()
                    .reversed() as MutableList<Friend>
            } else {
                friendList.sortedBy { it.user_name.lowercase(Locale.ROOT) }
                    .reversed() as MutableList<Friend>
            }
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun sortFriendsByCompany(){
        try {
            friendList = if (isSortedByCompany()) {
                friendList.sortedBy { if (it.bar_name == null) "" else it.bar_name.lowercase(Locale.ROOT) }
                    .reversed().reversed() as MutableList<Friend>
            } else {
                friendList.sortedBy { if (it.bar_name == null) "" else it.bar_name.lowercase(Locale.ROOT) }
                    .reversed() as MutableList<Friend>
            }
            notifyDataSetChanged()
        } catch (_: Exception) { }
    }

    private fun isSortedByName(): Boolean {
        sortedByName = sortedByName.not()
        return sortedByName
    }

    private fun isSortedByCompany(): Boolean {
        sortedByCompany = sortedByCompany.not()
        return sortedByCompany
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(users: MutableList<Friend>) {
        friendList = users
        notifyDataSetChanged()
    }
}