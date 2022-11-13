package com.example.zadanie.ui.login

import UserHandlerModel
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.zadanie.databinding.FragmentLoginBinding

import com.example.zadanie.data.ApiService
import com.example.zadanie.model.loggedInUser

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var userViewModel: UserHandlerModel

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
        userViewModel = ViewModelProvider(this)[UserHandlerModel::class.java]
        val apiService = ApiService()

        registerButton.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegistrationFragment()
            findNavController().navigate(action)
        }

        userViewModel.readUsers.observe(viewLifecycleOwner) { println(it) }

        loginButton.setOnClickListener {
            if(userName.isNotEmpty() && password.isNotEmpty()) {
                //val action = LoginFragmentDirections.actionLoginFragmentToCheckInDetailFragment(0)
                //val action = LoginFragmentDirections.actionLoginFragmentToCompanyFragment()
                val action = LoginFragmentDirections.actionLoginFragmentToAddFriendFragment()
                apiService.loginUser(userName.toString(), password.toString(), this, action)
            }
            else {
                Toast.makeText(context, "Enter a username and a password!", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
}