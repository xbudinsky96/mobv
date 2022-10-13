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

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val args: HomeFragmentArgs by navArgs()
    private val SEARCHPREFIX = "https://www.google.com/maps/@"

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val nameTitle: TextView = view.findViewById(R.id.nameTitle)
        val companyName: TextView = view.findViewById(R.id.companyName)
        val showOnMapButton: Button = view.findViewById(R.id.showOnMap)
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

        return view
    }
}