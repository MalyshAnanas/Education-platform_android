package com.hfad.digital_assistant

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.google.android.material.bottomsheet.BottomSheetDialog

class PracticumFragment : Fragment() {

    @SuppressLint("MissingInflatedId", "ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_practicum, container, false)
        val Case1 = view.findViewById<CardView>(R.id.CardCase1)
        val Case2 = view.findViewById<CardView>(R.id.CardCase2)
        val Case3 = view.findViewById<CardView>(R.id.CardCase3)
        val Case4 = view.findViewById<CardView>(R.id.CardCase4)

        Case1.setOnClickListener {
            Case1.setCardBackgroundColor(R.color.gray)
            showCasePopUp("Кейс 1", "КАКОЙ-ТО ОЧЕНЬ ВАЖНЫЙ ТЕКСТ ДЛЯ КЕЙСА 1")
        }
        Case2.setOnClickListener {
            Case2.setCardBackgroundColor(R.color.gray)
            showCasePopUp("Кейс 2", "КАКОЙ-ТО ОЧЕНЬ ВАЖНЫЙ ТЕКСТ ДЛЯ КЕЙСА 2")
        }
        Case3.setOnClickListener {
            Case3.setCardBackgroundColor(R.color.gray)
            showCasePopUp("Кейс 3", "КАКОЙ-ТО ОЧЕНЬ ВАЖНЫЙ ТЕКСТ ДЛЯ КЕЙСА 3")
        }
        Case4.setOnClickListener {
            Case4.setCardBackgroundColor(R.color.gray)
            showCasePopUp("Кейс 4", "КАКОЙ-ТО ОЧЕНЬ ВАЖНЫЙ ТЕКСТ ДЛЯ КЕЙСА 4")
        }

        return view
    }

    // Функция для вызова окна
    private fun showCasePopUp(title: String, description: String) {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val bottomSheetView = layoutInflater.inflate(R.layout.case_details, null)

        // Устанавливаем данные
        bottomSheetView.findViewById<TextView>(R.id.popUpTitleCase).text = title
        bottomSheetView.findViewById<TextView>(R.id.popUpCaseDescription).text = description

        dialog.setContentView(bottomSheetView)
        dialog.show()

    }
}