package com.hfad.digital_assistant.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hfad.digital_assistant.DocumentActivity
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.api.LibraryApi
import com.hfad.digital_assistant.model.local.LibraryDatabase
import com.hfad.digital_assistant.view.ProfileBottomSheet
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory

class RouteFragment : Fragment() {

    private lateinit var viewModel: RouteViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_route, container, false)
        val docsContainer = view.findViewById<LinearLayout>(R.id.docsContainer)

        // REPOSITORY И ROOM

        val api = LibraryApi.create()
        val remoteRepository = RemoteLibraryRepository(api)

        val database = LibraryDatabase.getInstance(requireContext())
        val libraryDao = database.libraryDao

        val factory = RouteViewModelFactory(remoteRepository, libraryDao)
        viewModel = ViewModelProvider(this, factory)[RouteViewModel::class.java]

        // Профиль пользователя

        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()
        val userNameText = view.findViewById<TextView>(R.id.userNameRoute)
        userNameText.text = fullName ?: "Гость"

        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRout)

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

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

        // 1. Сначала показываем локальные
        viewModel.loadLocalFiles()

        Log.i("RouteFragment", "bibika")
        // 2. Затем обновляем с сервера
        viewModel.refreshFiles()

        return view
    }

    private fun openDocument(doc: LibraryFile) {
        val intent = Intent(requireContext(), DocumentActivity::class.java)
        intent.putExtra("DOC_TITLE", doc.title)
        intent.putExtra("DOC_URL", doc.file)
        startActivity(intent)
    }
}