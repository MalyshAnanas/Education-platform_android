package com.hfad.digital_assistant

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.view.ProfileBottomSheet

class RouteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_route, container, false)
        val DOC1 = view.findViewById<LinearLayout>(R.id.DocAreaRoute1)
        val DOC2 = view.findViewById<LinearLayout>(R.id.DocAreaRoute2)
        val DOC3 = view.findViewById<LinearLayout>(R.id.DocAreaRoute3)
        val DOC4 = view.findViewById<LinearLayout>(R.id.DocAreaRoute4)
        val icon1 = view.findViewById<ImageView>(R.id.IconReadRoute1)
        val icon2 = view.findViewById<ImageView>(R.id.IconReadRoute2)
        val icon3 = view.findViewById<ImageView>(R.id.IconReadRoute3)
        val icon4 = view.findViewById<ImageView>(R.id.IconReadRoute4)

        DOC1.setOnClickListener{
            icon1.setImageResource(R.drawable.doc_read)
        }
        DOC2.setOnClickListener{
            icon2.setImageResource(R.drawable.doc_read)
        }
        DOC3.setOnClickListener{
            icon3.setImageResource(R.drawable.doc_read)
        }
        DOC4.setOnClickListener{
            icon4.setImageResource(R.drawable.doc_read)
        }

        // Получаем пользователя из Preferences
        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()
        val userNameText = view.findViewById<TextView>(R.id.userNameRoute)
        userNameText.text = fullName ?: "Гость"

        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRout)

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