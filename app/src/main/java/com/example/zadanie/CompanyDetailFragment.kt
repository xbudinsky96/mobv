package com.example.zadanie

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.databinding.FragmentCompanyDetailBinding

class CompanyDetailFragment : Fragment() {
    private lateinit var binding: FragmentCompanyDetailBinding
    private val args: CompanyDetailFragmentArgs by navArgs()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyDetailBinding.inflate(inflater, container, false)
        val companyTextDetail: TextView = binding.companyDetailText
        val openingHours = if(args.openingHours != null) "\n\nOtváracie hodiny: \n" + args.openingHours + "\n" else ""
        val tel = if(args.tel != null) "\n\nTelefónne čislo: \n" + args.tel + "\n" else ""
        val web = if(args.web != null) "\n\nWeb: \n" + args.web + "\n" else ""

        companyTextDetail.text =
                "${args.companyName}\n\n\n" +
                "Typ: ${args.type}" +
                openingHours + tel + web

        return binding.root
    }
}