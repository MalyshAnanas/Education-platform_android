package com.hfad.digital_assistant

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.view.ProfileBottomSheet


class MonitoringFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_monitoring, container, false)

        // Получаем пользователя из Preferences
        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()
        val userNameText = view.findViewById<TextView>(R.id.userNameMon)
        userNameText.text = fullName ?: "Гость"

        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerMon)

        // Открытие профиля
        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        return view
    }
}