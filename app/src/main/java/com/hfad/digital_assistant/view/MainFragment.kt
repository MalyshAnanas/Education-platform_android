package com.hfad.digital_assistant.view

import android.content.Intent
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
import com.hfad.digital_assistant.model.local.LibraryDatabase
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.viewModel.MainViewModel
import com.hfad.digital_assistant.viewModel.MainViewModelFactory
import com.hfad.digital_assistant.view.ProfileBottomSheet
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // ViewModel (если нужен)
        val database = LibraryDatabase.getInstance(requireContext())
        val libraryDao = database.libraryDao

        val factory = MainViewModelFactory(libraryDao)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val userNameText = view.findViewById<TextView>(R.id.userNameText)

        // Получаем пользователя из Preferences
        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()

        userNameText.text = fullName

        val docsContainer = view.findViewById<LinearLayout>(R.id.routeContainerMain)
        // Наблюдение за данными

        viewModel.libraryFiles.observe(viewLifecycleOwner) { files ->
            docsContainer.removeAllViews()
            files.forEach { file ->
                val docView = inflater.inflate(R.layout.doc_item_layout, docsContainer, false)

                val docName = docView.findViewById<TextView>(R.id.docName)
                val icon = docView.findViewById<ImageView>(R.id.iconRead)

                docName.text = file.title
                icon.setImageResource(R.drawable.doc_no_read)

                docView.setOnClickListener {
                    icon.setImageResource(R.drawable.doc_read)
                    openDocument(file)
                }

                docsContainer.addView(docView)
            }
        }

        // Загрузка данных

        // Показываем локальные
        viewModel.loadLocalFiles()

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

    private fun openDocument(doc: LibraryFile) {
        //TODO Сделать реализацию
    }

}
