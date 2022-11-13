package com.example.zadanie.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.zadanie.adapter.FriendsAdapter
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentFriendListBinding

class FriendListFragment : Fragment() {
    private var _binding: FragmentFriendListBinding? = null
    val binding get() = _binding!!
    private val adapter = FriendsAdapter(this)
    private val service = ApiService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)

        service.showFriends(this, adapter)

        return binding.root
    }
}