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
    private var dataIsSorted: Boolean = false
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val item = friendList[position]

        holder.friendName.text = item.user_name
        holder.frame.setOnClickListener {
            val action = FriendListFragmentDirections.actionFriendListFragmentToCompanyDetailFragment(
                item.bar_name,
                "",
                item.bar_lat,
                item.bar_lon,
                -1,
                item.user_id
            )
            fragment.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = friendList.size

    @SuppressLint("NotifyDataSetChanged")
    fun sortFriendsByName(){
        friendList = if(isSorted()) {
            friendList.sortedBy { it.user_name.lowercase(Locale.ROOT) }.reversed().reversed() as MutableList<Friend>
        } else {
            friendList.sortedBy { it.user_name.lowercase(Locale.ROOT) }.reversed() as MutableList<Friend>
        }
        notifyDataSetChanged()
    }

    private fun isSorted(): Boolean {
        dataIsSorted = dataIsSorted.not()
        return dataIsSorted
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(users: MutableList<Friend>) {
        friendList = users
        notifyDataSetChanged()
    }
}