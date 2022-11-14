package com.example.zadanie.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.zadanie.R
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!
    private val args: HomeFragmentArgs by navArgs()
    private val service = ApiService()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        service.getCompanyByID(null, this, args.companyId)
        binding.checkOut.setOnClickListener {
            service.checkOutCompany(this)
        }
        return binding.root
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
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.logout_app -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToLoginFragment())
            service.logoutUser(this)
            true
        }
        R.id.manage_friends -> {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToAddFriendFragment())
            true
        }
        else -> { super.onOptionsItemSelected(item) }
    }
}