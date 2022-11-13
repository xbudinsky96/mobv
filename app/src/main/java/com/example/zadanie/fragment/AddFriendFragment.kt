package com.example.zadanie.fragment

import UserHandlerModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentAddFriendBinding

class AddFriendFragment : Fragment() {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        val addButton = binding.addButton
        val friendsButton = binding.showFriends

        addButton.setOnClickListener {
            addFriend()
        }

        friendsButton.setOnClickListener {
            val action = AddFriendFragmentDirections.actionAddFriendFragmentToFriendListFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun addFriend() {
        val friendName = binding.friendName.text.toString()

        if (friendName.isNotEmpty()) {
            apiService.addFriend(friendName, this)
        }
        else {
            Toast.makeText(requireContext(), "Enter a name!", Toast.LENGTH_SHORT).show()
        }
    }
}