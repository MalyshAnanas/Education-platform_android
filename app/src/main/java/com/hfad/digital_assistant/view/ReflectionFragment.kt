package com.hfad.digital_assistant.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.view.LayoutInflater
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reflection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userPreferences = UserPreferences(requireContext())

        //  USER INFO
        val userNameText = view.findViewById<TextView>(R.id.userNameRef)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRef)

        userNameText.text = userPreferences.getFullName()

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        // VIEWMODEL
        viewModel = ViewModelProvider(
            this,
            ReflectionViewModelFactory(userPreferences)
        )[ReflectionViewModel::class.java]

        adapter = ReflectionAdapter(viewModel)

        // RECYCLER
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            isFocusable = false
            isFocusableInTouchMode = false
            adapter = this@ReflectionFragment.adapter
        }

        // UI ELEMENTS
        val btnSend = view.findViewById<Button>(R.id.btnSend)
        val btnToday = view.findViewById<Button>(R.id.btnToday)
        val btnPickDate = view.findViewById<Button>(R.id.btnPickDate)
        val progress = view.findViewById<View>(R.id.progressBar)
        val dateText = view.findViewById<TextView>(R.id.dateText)

        // OBSERVERS

        // список вопросов
        viewModel.questions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)

            if (list.isEmpty()) {
                dateText.text = "Данных по этому дню нет"
                dateText.visibility = View.VISIBLE
            }
        }

        // загрузка
        viewModel.isLoading.observe(viewLifecycleOwner) {
            progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        // состояние кнопки
        viewModel.isFormChanged.observe(viewLifecycleOwner) { changed ->
            val isToday = viewModel.isToday.value ?: true

            btnSend.isEnabled = changed && isToday
            btnSend.alpha = if (changed && isToday) 1f else 0.5f
        }

        // режим сегодня / не сегодня
        viewModel.isToday.observe(viewLifecycleOwner) { isToday ->
            adapter.setReadOnly(!isToday)
            val changed = viewModel.isFormChanged.value ?: false

            btnSend.isEnabled = changed && isToday
            btnSend.alpha = if (changed && isToday) 1f else 0.5f
        }

        // выбранная дата
        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            if (date != null) {
                dateText.visibility = View.VISIBLE
                dateText.text = "Дата: $date"
            } else {
                dateText.visibility = View.GONE
            }
        }

        // BUTTONS

        btnSend.setOnClickListener {
            viewModel.sendAnswers()

            // убрать фокус
            clearFocus(requireView())
        }

        btnToday.setOnClickListener {
            viewModel.loadToday()
        }

        btnPickDate.setOnClickListener {

            val cal = Calendar.getInstance()

            // фикс выбранной даты
            viewModel.selectedDate.value?.let { date ->
                val parts = date.split("-")
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }

            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val date = "$y-${m + 1}-$d"
                    viewModel.loadQuestionsForDate(date)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // INIT
        viewModel.loadQuestions()
    }

    private fun clearFocus(view: View) {
        view.clearFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE)
                as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}