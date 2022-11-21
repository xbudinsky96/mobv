package com.example.zadanie.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentFriendListBinding

class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendListBinding? = null
    val binding get() = _binding!!
    private val adapter = FriendsAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)
        val sortName = binding.byname
        val sortCompany = binding.bycompany

        sortName.setOnClickListener {
            adapter.sortFriendsByName()
        }

        sortCompany.setOnClickListener {
            adapter.sortFriendsByCompany()
        }

        apiService.showFriends(this, adapter)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}