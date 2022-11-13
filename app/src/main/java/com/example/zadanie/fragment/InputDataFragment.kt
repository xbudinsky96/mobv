package com.example.zadanie.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadanie.R
import com.example.zadanie.databinding.FragmentInputDataBinding
import com.example.zadanie.model.CompanyViewModel
import com.example.zadanie.model.Element
import com.example.zadanie.model.NearbyCompanyViewModel
import com.example.zadanie.model.Tags
import com.google.android.material.textfield.TextInputEditText

class InputDataFragment : Fragment(R.layout.fragment_input_data) {
    private lateinit var binding: FragmentInputDataBinding
    private lateinit var companyViewModel: CompanyViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInputDataBinding.inflate(inflater, container, false)
        companyViewModel = ViewModelProvider(this)[CompanyViewModel::class.java]
        val button: Button = binding.submitData
        val nameInput: TextInputEditText = binding.inputName
        val companyInput: TextInputEditText = binding.companyInput
        val longitude: TextInputEditText = binding.longitude
        val latitude: TextInputEditText = binding.latitude


        button.setOnClickListener{
            if(nameInput.text?.isNotEmpty() == true && companyInput.text?.isNotEmpty() == true) {
                val latVal = latitude.text.toString()
                val longVal = longitude.text.toString()
                val action = InputDataFragmentDirections.actionInputDataToHomeFragment(
                    nameInput.text.toString(),
                    companyInput.text.toString(),
                    longVal,
                    latVal
                )
                findNavController().navigate(action)
            }
            else{
                Toast.makeText(activity, "Prosím vyplňte údaje!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}