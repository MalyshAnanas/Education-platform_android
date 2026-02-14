package com.hfad.digital_assistant.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.UserPreferences

class ProfileBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.profile_details, container, false)

        val userPreferences = UserPreferences(requireContext())

        val userNameText = view.findViewById<TextView>(R.id.userNameTextProfile)
        val changePhoto = view.findViewById<TextView>(R.id.ChangePhoto)
        val exit = view.findViewById<TextView>(R.id.Exit)

        // Устанавливаем имя
        userNameText.text = userPreferences.getFullName() ?: "Гость"

        // Поменять фото
        changePhoto.setOnClickListener {
            Toast.makeText(requireContext(), "Функция смены фото", Toast.LENGTH_SHORT).show()
        }

        // Выйти из аккаунта
        exit.setOnClickListener {

            val userPreferences = UserPreferences(requireContext())
            userPreferences.clear()

            dismiss()

            requireActivity().apply {
                finish()
                startActivity(intent)
            }
        }

        return view
    }
}
