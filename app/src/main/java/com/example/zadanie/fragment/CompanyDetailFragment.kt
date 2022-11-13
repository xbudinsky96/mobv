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
        val mapButton: Button = binding.map

        val type = if(args.type != "") (args.type?.replace("_", " ") ?: args.type) + "\n\n" else ""
        val users = if(args.users != -1) "Users: " + args.users else ""

        companyTextDetail.text =
                "               ${args.companyName}\n\n" +
                type + users

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