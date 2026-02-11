package com.hfad.digital_assistant.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.api.AuthRepository
import com.hfad.digital_assistant.model.api.RetrofitClient
import com.hfad.digital_assistant.viewModel.LoginViewModel
import com.hfad.digital_assistant.viewModel.LoginViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val loginText = view.findViewById<EditText>(R.id.login)
        val passwordText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login_button)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)

        val userPreferences = UserPreferences(requireContext())
        val repository = AuthRepository(
            userPreferences = userPreferences,
            authApiService = RetrofitClient.create(userPreferences)
        )

        val factory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginResult.collectLatest { result ->
                result?.onSuccess {
                    findNavController().navigate(
                        R.id.action_loginFragment_to_mainFragment
                    )
                }?.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.isVisible = isLoading
                loginButton.isEnabled = !isLoading
            }
        }

        loginButton.setOnClickListener {
            val username = loginText.text.toString().trim()
            val password = passwordText.text.toString().trim()

            if (username.isEmpty()) {
                loginText.error = "Введите логин"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordText.error = "Введите пароль"
                return@setOnClickListener
            }

            viewModel.login(username, password)
        }

        return view
    }
}
