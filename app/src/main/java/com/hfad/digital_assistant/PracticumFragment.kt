package com.hfad.digital_assistant

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView

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

        Case1.setOnClickListener{
            Case1.setCardBackgroundColor(R.color.gray)
        }
        Case2.setOnClickListener{
            Case2.setCardBackgroundColor(R.color.gray)
        }
        Case3.setOnClickListener{
            Case3.setCardBackgroundColor(R.color.gray)
        }
        Case4.setOnClickListener{
            Case4.setCardBackgroundColor(R.color.gray)
        }

        return view
    }

}