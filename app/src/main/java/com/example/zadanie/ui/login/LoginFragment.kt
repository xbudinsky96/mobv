package com.example.zadanie.ui.login

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val userName = binding.username.text
        val password = binding.passwordLogin.text
        val loginButton = binding.register2Button
        val registerButton = binding.registerButton

        apiService.getLoggedUser(this)
        getLocation()

        registerButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()
            findNavController().navigate(action)
        }

        loginButton.setOnClickListener {
            if(userName.isNotEmpty() && password.isNotEmpty()) {
                apiService.loginUser(userName.toString(), password.toString(), this)
            }
            else {
                Toast.makeText(context, "Enter a username and a password!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}