package com.example.zadanie.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
}