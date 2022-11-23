package com.example.zadanie.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.zadanie.api.apiService
import com.example.zadanie.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

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
                Snackbar.make(this.requireView(), "Enter a username and a password!", Snackbar.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}