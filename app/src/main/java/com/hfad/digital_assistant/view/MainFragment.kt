package com.hfad.digital_assistant.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.MainViewModel
import com.hfad.digital_assistant.viewModel.MainViewModelFactory
import com.hfad.digital_assistant.view.ProfileBottomSheet


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        val doc1 = view.findViewById<LinearLayout>(R.id.DocArea1)
        val doc2 = view.findViewById<LinearLayout>(R.id.DocArea2)
        val doc3 = view.findViewById<LinearLayout>(R.id.DocArea3)

        val icon1 = view.findViewById<ImageView>(R.id.IconRead1)
        val icon2 = view.findViewById<ImageView>(R.id.IconRead2)
        val icon3 = view.findViewById<ImageView>(R.id.IconRead3)

        val userNameText = view.findViewById<TextView>(R.id.userNameText)

        // Получаем пользователя из Preferences
        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()

        userNameText.text = fullName ?: "Гость"

        // Клики
        doc1.setOnClickListener {
            icon1.setImageResource(R.drawable.doc_read)
        }

        doc2.setOnClickListener {
            icon2.setImageResource(R.drawable.doc_read)
        }

        doc3.setOnClickListener {
            icon3.setImageResource(R.drawable.doc_read)
        }

        // ViewModel (если нужен)
        val factory = MainViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainer)

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
