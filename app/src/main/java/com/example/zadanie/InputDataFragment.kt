package com.example.zadanie

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class InputDataFragment : Fragment(R.layout.fragment_input_data) {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_input_data, container, false)
        val button: Button = view.findViewById(R.id.submitData)
        val nameInput: TextInputEditText = view.findViewById(R.id.inputName)
        val companyInput: TextInputEditText = view.findViewById(R.id.companyInput)
        val longitude: TextInputEditText = view.findViewById(R.id.longitude)
        val latitude: TextInputEditText = view.findViewById(R.id.latitude)


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

        return view
    }
}