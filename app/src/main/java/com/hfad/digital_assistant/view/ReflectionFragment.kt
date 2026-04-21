package com.hfad.digital_assistant.view

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.ReflectionViewModel
import com.hfad.digital_assistant.viewModel.ReflectionViewModelFactory
import java.util.Calendar

class ReflectionFragment : Fragment() {

    private lateinit var viewModel: ReflectionViewModel
    private lateinit var adapter: ReflectionAdapter
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reflection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())

        val userNameText = view.findViewById<TextView>(R.id.userNameRef)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRef)

        userNameText?.text = userPreferences.getFullName()

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }
        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        viewModel = ViewModelProvider(
            this,
            ReflectionViewModelFactory(userPreferences)
        )[ReflectionViewModel::class.java]

        updateUserHeader(view)

        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, _ ->
            updateUserHeader(view)
        }

        adapter = ReflectionAdapter(viewModel)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            setHasFixedSize(false)
            isNestedScrollingEnabled = false
            adapter = this@ReflectionFragment.adapter
        }

        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val btnToday = view.findViewById<Button>(R.id.btnToday)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickDate)
        val progress = view.findViewById<View>(R.id.progressBar)
        val dateText = view.findViewById<TextView>(R.id.dateText)

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)

            if (list.isEmpty()) {
                dateText.visibility = View.VISIBLE
                dateText.text = "Данных по этому дню нет"
            } else {
                val date = viewModel.selectedDate.value
                if (date != null) {
                    dateText.visibility = View.VISIBLE
                    dateText.text = "Дата: $date"
                } else {
                    dateText.visibility = View.GONE
                }
            }

            updateButtonState(btnSend)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.isEditable.observe(viewLifecycleOwner) { editable ->
            adapter.setReadOnly(!editable)
            updateButtonState(btnSend)
        }

        viewModel.buttonText.observe(viewLifecycleOwner) { text ->
            btnSend.text = text
        }

        viewModel.isFormChanged.observe(viewLifecycleOwner) {
            updateButtonState(btnSend)
        }

        viewModel.isToday.observe(viewLifecycleOwner) {
            updateButtonState(btnSend)
        }

        viewModel.hasAnswersForCurrentDay.observe(viewLifecycleOwner) {
            updateButtonState(btnSend)
        }

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (viewModel.questions.value.isNullOrEmpty()) {
                dateText.visibility = View.VISIBLE
                dateText.text = "Данных по этому дню нет"
            } else {
                dateText.visibility = View.VISIBLE
                dateText.text = "Дата: $date"
            }
        }

        btnSend.setOnClickListener {
            clearFocus(requireView())
            viewModel.onMainButtonClicked()
        }

        btnToday.setOnClickListener {
            viewModel.loadToday()
        }

        btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()

            viewModel.selectedDate.value?.let { date ->
                val parts = date.split("-")
                if (parts.size == 3) {
                    cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                }
            }

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val month = (m + 1).toString().padStart(2, '0')
                    val day = d.toString().padStart(2, '0')
                    val date = "$y-$month-$day"
                    viewModel.loadQuestionsForDate(date)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        viewModel.loadQuestions()
    }

    private fun updateButtonState(btnSend: Button) {
        val isToday = viewModel.isToday.value == true
        val isEditable = viewModel.isEditable.value == true
        val isChanged = viewModel.isFormChanged.value == true
        val hasAnswers = viewModel.hasAnswersForCurrentDay.value == true

        val enabled = when {
            !isToday -> false
            isEditable && hasAnswers -> isChanged
            isEditable && !hasAnswers -> isChanged
            !isEditable && hasAnswers -> true
            else -> false
        }

        btnSend.isEnabled = enabled
        btnSend.alpha = if (enabled) 1f else 0.5f
    }

    private fun clearFocus(view: View) {
        view.clearFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // Для обнавления шапки пользователя на странице
    private fun updateUserHeader(view: View) {
        val userNameText = view.findViewById<TextView>(R.id.userNameRef)
        val userPhoto = view.findViewById<ImageView>(R.id.userImageRef)

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