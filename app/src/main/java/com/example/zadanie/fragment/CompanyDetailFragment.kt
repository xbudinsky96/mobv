package com.example.zadanie.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.example.zadanie.databinding.FragmentCompanyDetailBinding

class CompanyDetailFragment : Fragment() {
    private lateinit var binding: FragmentCompanyDetailBinding
    private val args: CompanyDetailFragmentArgs by navArgs()
    private val SEARCHPREFIX = "https://www.google.com/maps/@"

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompanyDetailBinding.inflate(inflater, container, false)
        val companyTextDetail: TextView = binding.companyDetailText
        val webButton: Button = binding.web
        val telButton: Button = binding.telefon
        val mapButton: Button = binding.map

        val openingHours = if(args.openingHours != "") "\n\nOpening hours: \n" + args.openingHours + "\n" else ""
        val tel = if(args.tel != "") "\n\nPhone number: \n" + args.tel + "\n" else ""
        val web = if(args.web != "") "\n\nWeb: \n" + args.web + "\n" else ""

        companyTextDetail.text =
                "${args.companyName}\n\n\n" +
                "Type: ${args.type}" +
                openingHours + tel + web

        webButton.setOnClickListener {
            if(web != "") {
                val pageUrl = Uri.parse(args.web)
                val goToWeb = Intent(Intent.ACTION_VIEW, pageUrl)
                startActivity(goToWeb)
            }
            else {
                Toast.makeText(activity, "No webpage provided!", Toast.LENGTH_SHORT).show()
            }
        }

        telButton.setOnClickListener {
            if(tel != "") {
                val phoneNumber = Uri.parse("tel:$args.tel")
                println(args.tel)
                val makeCall = Intent(Intent.ACTION_DIAL, phoneNumber)
                startActivity(makeCall)
            }
            else {
                Toast.makeText(activity, "No phone number provided!", Toast.LENGTH_SHORT).show()
            }
        }

        mapButton.setOnClickListener {
            val latitude = args.lat
            val longitude = args.lon

            if(latitude.isNotEmpty() && longitude.isNotEmpty()) {
                val queryUrl: Uri = Uri.parse("${SEARCHPREFIX}${latitude},${longitude},16z")
                val showOnMap = Intent(Intent.ACTION_VIEW, queryUrl)
                startActivity(showOnMap)
            }
            else{
                Toast.makeText(activity, "No location provided!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}