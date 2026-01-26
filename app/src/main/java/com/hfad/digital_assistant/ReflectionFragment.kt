package com.hfad.digital_assistant

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class ReflectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reflection, container, false)

        // Пример для одного блока иконок
        val emojis1 = listOf(
            view.findViewById<ImageView>(R.id.excellent1),
            view.findViewById<ImageView>(R.id.good1),
            view.findViewById<ImageView>(R.id.normally1),
            view.findViewById<ImageView>(R.id.bad1),
            view.findViewById<ImageView>(R.id.terribly1)
        )

        val emojis2 = listOf(
            view.findViewById<ImageView>(R.id.excellent2),
            view.findViewById<ImageView>(R.id.good2),
            view.findViewById<ImageView>(R.id.normally2),
            view.findViewById<ImageView>(R.id.bad2),
            view.findViewById<ImageView>(R.id.terribly2)
        )

        val emojis3 = listOf(
            view.findViewById<ImageView>(R.id.excellent3),
            view.findViewById<ImageView>(R.id.good3),
            view.findViewById<ImageView>(R.id.normally3),
            view.findViewById<ImageView>(R.id.bad3),
            view.findViewById<ImageView>(R.id.terribly3)
        )

        emojis1.forEach { selectedView ->
            selectedView.setOnClickListener {
                emojis1.forEach { otherView ->
                    if (otherView == selectedView) {
                        otherView.alpha = 1.0f // Выбранная — яркая
                    } else {
                        otherView.alpha = 0.2f // Остальные — 80% прозрачности
                    }
                }
            }
        }

        emojis2.forEach { selectedView ->
            selectedView.setOnClickListener {
                emojis2.forEach { otherView ->
                    if (otherView == selectedView) {
                        otherView.alpha = 1.0f // Выбранная — яркая
                    } else {
                        otherView.alpha = 0.2f // Остальные — 80% прозрачности
                    }
                }
            }
        }

        emojis3.forEach { selectedView ->
            selectedView.setOnClickListener {
                emojis3.forEach { otherView ->
                    if (otherView == selectedView) {
                        otherView.alpha = 1.0f // Выбранная — яркая
                    } else {
                        otherView.alpha = 0.2f // Остальные — 80% прозрачности
                    }
                }
            }
        }

        return view
    }
}