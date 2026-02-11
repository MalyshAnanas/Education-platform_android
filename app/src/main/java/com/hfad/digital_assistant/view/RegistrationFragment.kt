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
import com.hfad.digital_assistant.model.api.AuthRepository
import com.hfad.digital_assistant.model.api.RetrofitClient
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.RegistrationViewModel
import com.hfad.digital_assistant.viewModel.RegistrationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RegistrationFragment : Fragment() {

    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_registration, container, false)

        val userloginText = view.findViewById<EditText>(R.id.login)
        val passwordText = view.findViewById<EditText>(R.id.password)
        val emailText = view.findViewById<EditText>(R.id.email)
        val fullNameText = view.findViewById<EditText>(R.id.full_name)

        val sendButton = view.findViewById<Button>(R.id.send)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressbar)

        val goToLoginText = view.findViewById<TextView>(R.id.go_to_login)
        goToLoginText.setOnClickListener {
            findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
        }


        // Репозиторий
        val userPreferences = UserPreferences(requireContext())
        val repository = AuthRepository(
            userPreferences = userPreferences,
            authApiService = RetrofitClient.create(userPreferences)
        )

        val factory = RegistrationViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[RegistrationViewModel::class.java]

        // Слушаем результат регистрации
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationResult.collectLatest { result ->
                result?.onSuccess { userData ->

                    val action =
                        RegistrationFragmentDirections
                            .actionRegistrationFragmentToMainFragment()

                    findNavController().navigate(action)

                }?.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                progressBar.isVisible = isLoading
                sendButton.isEnabled = !isLoading
            }
        }

        sendButton.setOnClickListener {

            val username = userloginText.text.toString().trim()
            val password = passwordText.text.toString().trim()
            val email = emailText.text.toString().trim()
            val fullName = fullNameText.text.toString().trim()

            var isValid = true

            if (username.isEmpty()) {
                userloginText.error = "Введите логин"
                isValid = false
            }

            if (password.isEmpty()) {
                passwordText.error = "Введите пароль"
                isValid = false
            }

            if (email.isEmpty()) {
                emailText.error = "Введите email"
                isValid = false
            }

            if (fullName.isEmpty()) {
                fullNameText.error = "Введите ФИО"
                isValid = false
            }

            if (isValid) {
                viewModel.register(username, password, email, fullName)
            }
        }

        return view
    }
}
