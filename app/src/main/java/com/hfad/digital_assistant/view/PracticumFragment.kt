package com.hfad.digital_assistant.view

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.CaseUiStatus
import com.hfad.digital_assistant.model.api.PracticumCaseUi
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.PracticumViewModel
import com.hfad.digital_assistant.viewModel.PracticumViewModelFactory

class PracticumFragment : Fragment() {

    private lateinit var viewModel: PracticumViewModel
    private lateinit var adapter: CaseAdapter
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_practicum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())

        // USER INFO
        val userNameText = view.findViewById<TextView>(R.id.userNamePrac)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerPrac)
        val statsText = view.findViewById<TextView>(R.id.completedCasesText) // 👈 ВАЖНО

        userNameText.text = userPreferences.getFullName()

        // Открытие профиля

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }
        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        // VIEWMODEL
        viewModel = ViewModelProvider(
            this,
            PracticumViewModelFactory(userPreferences)
        )[PracticumViewModel::class.java]

        updateUserHeader(view)

        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, _ ->
            updateUserHeader(view)
        }

        // ADAPTER
        adapter = CaseAdapter { case ->
            showCaseBottomSheet(case)
        }

        // RECYCLER
        val recycler = view.findViewById<RecyclerView>(R.id.casesRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        recycler.adapter = adapter

        observeViewModel(statsText)

        viewModel.loadCases()
    }

    private fun observeViewModel(statsText: TextView) {

        // список кейсов
        viewModel.cases.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        // СЧЁТЧИК
        viewModel.stats.observe(viewLifecycleOwner) { (done, notDone) ->
            statsText.text =
                "Выполнено: $done кейса\nНе выполнено: $notDone кейса"
        }
    }

    private fun showCaseBottomSheet(case: PracticumCaseUi) {

        val dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.case_details, null)

        val title = view.findViewById<TextView>(R.id.popUpTitleCase)
        val desc = view.findViewById<TextView>(R.id.popUpCaseDescription)
        val input = view.findViewById<EditText>(R.id.inputAnswer)
        val btn = view.findViewById<Button>(R.id.btnSend)
        val adminCommentView = view.findViewById<TextView>(R.id.adminComment)
        val statusText = view.findViewById<TextView>(R.id.statusText)

        dialog.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }

        // Данные кейса
        title.text = case.name
        desc.text = case.description

        // Комментарий администратора
        if (!case.adminComment.isNullOrEmpty()) {
            adminCommentView.visibility = View.VISIBLE
            adminCommentView.text =
                "Комментарий администратора:\n${case.adminComment}"
        } else {
            adminCommentView.visibility = View.GONE
        }

        // ЛОГИКА СТАТУСА
        when (case.status) {

            CaseUiStatus.OPEN -> {
                statusText.visibility = View.GONE

                input.setText("")
                input.isEnabled = true

                btn.text = "Отправить"
                btn.isEnabled = true

                btn.setOnClickListener {
                    viewModel.sendAnswer(
                        case.id,
                        input.text.toString()
                    )
                    dialog.dismiss()
                }
            }

            CaseUiStatus.CHECKING -> {
                statusText.visibility = View.VISIBLE
                statusText.text = "На проверке"

                input.setText(case.userAnswer)
                input.isEnabled = false

                btn.text = "На проверке"
                btn.isEnabled = false
            }

            CaseUiStatus.DONE -> {
                statusText.visibility = View.VISIBLE
                statusText.text = "Проверено"

                input.setText(case.userAnswer)
                input.isEnabled = false

                btn.text = "Отправлено"
                btn.isEnabled = false
            }
        }

        dialog.setContentView(view)
        dialog.show()
    }

    // Для обнавления шапки пользователя на странице
    private fun updateUserHeader(view: View) {
        val userNameText = view.findViewById<TextView>(R.id.userNamePrac)
        val userPhoto = view.findViewById<ImageView>(R.id.userImagePrac)

        val fullName = userPreferences.getFullName()
        val serverPhotoUrl = userPreferences.getServerPhotoUrl()
        val localPhotoUri = userPreferences.getPhotoUri()

        userNameText.text = fullName ?: "Гость"

        when {
            !serverPhotoUrl.isNullOrBlank() -> {
                // Если используешь Coil:
                userPhoto.load(serverPhotoUrl) {
                    placeholder(R.drawable.kuromi)
                    error(R.drawable.kuromi)
                }
            }

            !localPhotoUri.isNullOrBlank() -> {
                userPhoto.setImageURI(Uri.parse(localPhotoUri))
            }

            else -> {
                userPhoto.setImageResource(R.drawable.kuromi)
            }
        }
    }
}