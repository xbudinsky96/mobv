package com.example.zadanie.ui.login

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.zadanie.data.ApiService
import com.example.zadanie.databinding.FragmentRegistrationBinding

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        val apiService = ApiService()

        registerButton.setOnClickListener {
            val userNameString = userName.toString()
            val passString = password.toString()
            val passCheckString = passwordVerify.toString()

            if(userNameString.isNotEmpty() && passString.isNotEmpty() && passCheckString.isNotEmpty()) {
                if(passString == passCheckString) {
                    apiService.registerUser(userNameString, passString, this)
                }
                else {
                    Toast.makeText(context, "Passwords don't match!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(context, "Enter a username and a password!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}