package com.hfad.digital_assistant.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.viewModel.MainViewModel
import com.hfad.digital_assistant.viewModel.MainViewModelFactory
import com.hfad.digital_assistant.R

class MainFragment : Fragment() {

    lateinit var viewModel: MainViewModel
    lateinit var viewModelFactory: MainViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        val DOC1 = view.findViewById<LinearLayout>(R.id.DocArea1)
        val DOC2 = view.findViewById<LinearLayout>(R.id.DocArea2)
        val DOC3 = view.findViewById<LinearLayout>(R.id.DocArea3)
        val icon1 = view.findViewById<ImageView>(R.id.IconRead1)
        val icon2 = view.findViewById<ImageView>(R.id.IconRead2)
        val icon3 = view.findViewById<ImageView>(R.id.IconRead3)

        DOC1.setOnClickListener{
            icon1.setImageResource(R.drawable.doc_read)
        }
        DOC2.setOnClickListener{
            icon2.setImageResource(R.drawable.doc_read)
        }
        DOC3.setOnClickListener{
            icon3.setImageResource(R.drawable.doc_read)
        }

        viewModelFactory = MainViewModelFactory()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return view
    }
}