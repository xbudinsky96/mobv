package com.example.zadanie

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
import com.example.zadanie.databinding.FragmentCompanyBinding
import com.example.zadanie.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private val args: HomeFragmentArgs by navArgs()
    private val SEARCHPREFIX = "https://www.google.com/maps/@"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val nameTitle: TextView = binding.nameTitle
        val companyName: TextView = binding.companyName
        val showOnMapButton: Button = binding.showOnMap
        val latitude = args.latitude
        val longitude = args.longitude

        nameTitle.text = args.name
        companyName.text = args.companyName

        //47.97530277530897, 18.15310231159761
        showOnMapButton.setOnClickListener {
            if(latitude != null && latitude.isNotEmpty() && longitude != null && longitude.isNotEmpty()) {
                val queryUrl: Uri = Uri.parse("${SEARCHPREFIX}${longitude},${latitude},12z")
                val intent = Intent(Intent.ACTION_VIEW, queryUrl)
                startActivity(intent)
            }
            else{
                Toast.makeText(activity, "Nezadali ste údaje polohy!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}