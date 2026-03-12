package com.hfad.digital_assistant.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.api.LibraryApi
import com.hfad.digital_assistant.model.local.LibraryDatabase
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

class RouteFragment : Fragment() {

    private lateinit var viewModel: RouteViewModel
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                handleSelectedFile(it)
            }
        }
    private lateinit var userPreferences: UserPreferences
    private lateinit var api: LibraryApi


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_route, container, false)
        val docsContainer = view.findViewById<LinearLayout>(R.id.docsContainer)

        // REPOSITORY И ROOM
        userPreferences = UserPreferences(requireContext())
        api = LibraryApi.create(userPreferences)
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
            updateFileCounters()

            files.forEach { file ->
                val docView = inflater.inflate(R.layout.doc_item_layout, docsContainer, false)

                val docName = docView.findViewById<TextView>(R.id.docName)
                val icon = docView.findViewById<ImageView>(R.id.iconRead)

                docName.text = file.title
                icon.setImageResource(R.drawable.doc_no_read)

                docView.setOnClickListener {

                    val localPath = file.localPath

                    if (!localPath.isNullOrEmpty() && File(localPath).exists()) {

                        // файл уже скачан
                        openLocalDocument(localPath)

                    } else {
                        icon.setImageResource(R.drawable.doc_read)

                        Toast.makeText(requireContext(), "Скачивание документа...", Toast.LENGTH_SHORT).show()

                        viewModel.downloadFile(file, requireContext()) { downloadedFile ->
                            openLocalDocument(downloadedFile.localPath!!)
                        }
                    }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val downloadButton = view.findViewById<View>(R.id.downloadButton)

        // Обработка нажатия на кнопку
        downloadButton.setOnClickListener {
            openFilePicker()
        }
    }

    // Функция склонения
    private fun getDocumentsWord(count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100

        return when {
            mod10 == 1 && mod100 != 11 -> "документ"
            mod10 in 2..4 && mod100 !in 12..14 -> "документа"
            else -> "документов"
        }
    }

    // Метод открытия проводника
    private fun openFilePicker() {
        filePickerLauncher.launch(arrayOf("*/*"))
    }

    // Обработка выбранного файла
    private fun handleSelectedFile(uri: Uri) {
        try {
            // Даём приложению доступ к выбранному файлу
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Получаем реальное имя файла
        val fileName = getFileName(uri) ?: "document_${System.currentTimeMillis()}"

        // Вызываем ViewModel для загрузки
        lifecycleScope.launch {
            try {
                viewModel.uploadFile(uri, fileName, requireContext())

                Toast.makeText(requireContext(), "Файл загружен", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    /** Метод для обновления счетчиков загруженных и всех файлов */
    private fun updateFileCounters() {
        // Загруженные пользователем
        val downloadedFiles = viewModel.libraryFiles.value?.filter { it.author_name == "Пользователь" } ?: emptyList()
        val downloadedCount = downloadedFiles.size
        val downloadFilesCounter = view?.findViewById<TextView>(R.id.downloadFilesCounter)
        val downloadFilesDoc = view?.findViewById<TextView>(R.id.downloadFilesDoc)
        downloadFilesCounter?.text = downloadedCount.toString()
        downloadFilesDoc?.text = getDocumentsWord(downloadedCount)

        // Все файлы
        val allFiles = viewModel.libraryFiles.value ?: emptyList()
        val allCount = allFiles.size
        val allFilesCounter = view?.findViewById<TextView>(R.id.allFilesCounter)
        val allFilesDoc = view?.findViewById<TextView>(R.id.allFilesDoc)
        allFilesCounter?.text = allCount.toString()
        allFilesDoc?.text = getDocumentsWord(allCount)
    }

    // Получение имени
    private fun getFileName(uri: Uri): String {
        var name = "file"

        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                name = it.getString(index)
            }
        }

        return name
    }

    // Метод открытия файла локально
    private fun openLocalDocument(path: String) {

        val file = File(path)

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "*/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            startActivity(Intent.createChooser(intent, "Открыть документ"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Нет приложения для открытия файла", Toast.LENGTH_SHORT).show()
        }
    }
}