package com.hfad.digital_assistant.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_splash, container, false)

        userPreferences = UserPreferences(requireContext())

        // Делаем задержку, чтобы показать Splash, или сразу проверяем
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500) // небольшая пауза, можно убрать

            if (userPreferences.isAuthorized()) {
                // Пользователь уже авторизован
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToMainFragment())
            } else {
                // Пользователь не авторизован
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToRegistrationFragment())
            }
        }

        return view
    }
}

