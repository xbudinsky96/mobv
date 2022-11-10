package com.example.zadanie.fragment

import UserHandlerModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentAddFriendBinding

class AddFriendFragment : Fragment() {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiService()
    private lateinit var userHandlerModel: UserHandlerModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFriendBinding.inflate(inflater, container, false)
        userHandlerModel = ViewModelProvider(this)[UserHandlerModel::class.java]
        val addButton = binding.addButton

        addButton.setOnClickListener {
            addFriend()
        }
        return binding.root
    }

    private fun addFriend() {
        val friendName = binding.friendName.toString()

        if (friendName.isNotEmpty()) {
            apiService.addFriend(friendName, userHandlerModel, this)
        }
        else {
            Toast.makeText(requireContext(), "Enter a name!", Toast.LENGTH_SHORT).show()
        }
    }
}