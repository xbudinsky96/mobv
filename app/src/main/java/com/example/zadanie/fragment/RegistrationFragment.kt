package com.example.zadanie.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentRegistrationBinding
import com.google.android.material.snackbar.Snackbar

class RegistrationFragment : Fragment() {
    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        val userName = binding.username.text
        val password = binding.passwordLogin.text
        val passwordVerify = binding.passwordCheck.text
        val registerButton = binding.register2Button

        registerButton.setOnClickListener {
            val userNameString = userName.toString()
            val passString = password.toString()
            val passCheckString = passwordVerify.toString()

            if(userNameString.isNotEmpty() && passString.isNotEmpty() && passCheckString.isNotEmpty()) {
                if(passString == passCheckString) {
                    apiService.registerUser(userNameString, passString, this)
                }
                else {
                    Snackbar.make(requireView(), "Passwords don't match!", Snackbar.LENGTH_SHORT).show()
                }
            }
            else {
                Snackbar.make(requireView(), "Enter a username and a password!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}