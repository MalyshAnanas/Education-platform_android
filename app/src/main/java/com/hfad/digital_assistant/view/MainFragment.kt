package com.hfad.digital_assistant.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.hfad.digital_assistant.model.api.LibraryApi
import com.hfad.digital_assistant.model.api.RemoteCurrentRepository
import com.hfad.digital_assistant.model.api.RemoteLibraryRepository
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.local.CurrentApi
import com.hfad.digital_assistant.model.local.LibraryDatabase
import com.hfad.digital_assistant.model.local.LibraryFile
import com.hfad.digital_assistant.viewModel.MainViewModel
import com.hfad.digital_assistant.viewModel.MainViewModelFactory
import com.hfad.digital_assistant.view.ProfileBottomSheet
import com.hfad.digital_assistant.viewModel.RouteViewModel
import com.hfad.digital_assistant.viewModel.RouteViewModelFactory
import kotlinx.coroutines.launch
import java.io.File


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                handleSelectedFile(it)
            }
        }
    private lateinit var userPreferences: UserPreferences
    private lateinit var api: LibraryApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // REPOSITORY И ROOM
        userPreferences = UserPreferences(requireContext())

        val libraryApi = LibraryApi.create(userPreferences)
        val currentApi = CurrentApi.create(userPreferences)
        val remoteLibraryRepository = RemoteLibraryRepository(libraryApi)
        val remoteCurrentRepository = RemoteCurrentRepository(currentApi)


        // ViewModel (если нужен)
        val database = LibraryDatabase.getInstance(requireContext())
        val libraryDao = database.libraryDao

        val factory = MainViewModelFactory(
            remoteLibraryRepository,
            remoteCurrentRepository,
            libraryDao
        )
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        updateUserHeader(view)

        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, _ ->
            updateUserHeader(view)
        }

        // Получаем пользователя из Preferences
        val userPreferences = UserPreferences(requireContext())
        val fullName = userPreferences.getFullName()

        //Цели
        val goalEditText = view.findViewById<EditText>(R.id.goalEditText)
        val saveButton = view.findViewById<Button>(R.id.saveGoalButton)
        val localGoal = userPreferences.getGoal()
        if (!localGoal.isNullOrEmpty()) {
            goalEditText.setText(localGoal)
        }

        // загрузка цели
        viewModel.loadGoalFromServer(
            onGoalLoaded = { goal ->
                goalEditText.setText(goal)
                userPreferences.saveGoal(goal ?: "")
            },
            onQuoteLoaded = { quote ->
                Log.d("QUOTE_DEBUG", "Quote: $quote")
                goalEditText.setText(quote)
            }
        )

        // сохранение
        saveButton.setOnClickListener {
            val goal = goalEditText.text.toString().trim()

            if (goal.isEmpty()) {
                // Удаляем цель
                userPreferences.clearGoal()

                viewModel.deleteGoalFromServer {
                    Toast.makeText(requireContext(), "Цель удалена", Toast.LENGTH_SHORT).show()
                }

            } else {
                // Сохраняем цель
                userPreferences.saveGoal(goal)

                viewModel.syncGoalToServer(goal) {
                    Toast.makeText(requireContext(), "Цель сохранена", Toast.LENGTH_SHORT).show()
                }
            }
        }

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

        // Открытие профиля
        val userNameText = view.findViewById<TextView>(R.id.userNameText)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainer)

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.text = fullName
        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }


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

    // Для обнавления шапки пользователя на странице
    private fun updateUserHeader(view: View) {
        val userNameText = view.findViewById<TextView>(R.id.userNameText)
        val userPhoto = view.findViewById<ImageView>(R.id.User_photo)

        val fullName = userPreferences.getFullName()
        val photoUriString = userPreferences.getPhotoUri()

        userNameText.text = fullName ?: "Гость"

        if (!photoUriString.isNullOrBlank()) {
            try {
                userPhoto.setImageURI(Uri.parse(photoUriString))
            } catch (e: Exception) {
                userPhoto.setImageResource(R.drawable.kuromi)
            }
        } else {
            userPhoto.setImageResource(R.drawable.kuromi)
        }
    }

}
