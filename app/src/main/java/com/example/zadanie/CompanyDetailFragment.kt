package com.example.zadanie

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs

class CompanyDetailFragment : Fragment() {
    private val args: CompanyDetailFragmentArgs by navArgs()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_company_detail, container, false)
        val companyTextDetail: TextView = view.findViewById(R.id.companyDetailText)
        val openingHours = if(args.openingHours != null) "\n\nOtváracie hodiny: \n" + args.openingHours + "\n" else ""
        val tel = if(args.tel != null) "\n\nTelefónne čislo: \n" + args.tel + "\n" else ""
        val web = if(args.web != null) "\n\nWeb: \n" + args.web + "\n" else ""

        companyTextDetail.text =
                "${args.companyName}\n\n\n" +
                "Typ: ${args.type}" +
                openingHours + tel + web

        return view
    }
}