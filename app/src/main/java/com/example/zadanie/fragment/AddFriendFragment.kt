package com.example.zadanie.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        val removeButton = binding.removeButton
        val friendName = binding.friendName

        addButton.setOnClickListener {
            addFriend(friendName.text.toString())
        }

        removeButton.setOnClickListener {
            removeFriend(friendName.text.toString())
        }

        friendsButton.setOnClickListener {
            val action = AddFriendFragmentDirections.actionAddFriendFragmentToFriendListFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    private fun addFriend(name: String) {
        if (validate(name)) {
            apiService.addFriend(name, this)
        }
        else {
            Toast.makeText(requireContext(), "Enter a name!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeFriend(name: String) {
        if (validate(name)) {
            apiService.deleteFriend(name, this)
        }
        else {
            Toast.makeText(requireContext(), "Enter a name!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validate(field: String): Boolean {
        if (field.isNotEmpty()) {
            return true
        }
        return false
    }
}