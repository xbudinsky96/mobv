package com.example.zadanie.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.zadanie.R
import com.example.zadanie.data.ApiService
import com.example.zadanie.data.apiService
import com.example.zadanie.databinding.FragmentAddFriendBinding
import com.example.zadanie.model.loggedInUser

class AddFriendFragment : Fragment() {
    private var _binding: FragmentAddFriendBinding? = null
    private val binding get() = _binding!!

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("inflater.inflate(R.menu.menuicons, menu)", "com.example.zadanie.R")
    )
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menuicons, menu)
        menu.findItem(R.id.manage_friends).isVisible = false
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(AddFriendFragmentDirections.actionAddFriendFragmentToLoginFragment())
            apiService.logoutUser(this)
            true
        }
        R.id.my_company -> {
            try {
                findNavController().navigate(AddFriendFragmentDirections.actionAddFriendFragmentToHomeFragment(loggedInUser.companyId?.toLong()!!))
            }
            catch (e: Exception) {
                Toast.makeText(requireContext(), "You are not checked in!", Toast.LENGTH_SHORT).show()
            }
            true
        }
        R.id.check_in -> {
            findNavController().navigate(AddFriendFragmentDirections.actionAddFriendFragmentToCheckInDetailFragment(0))
            true
        }
        R.id.companies_with_members -> {
            findNavController().navigate(AddFriendFragmentDirections.actionAddFriendFragmentToCompanyFragment())
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }
}