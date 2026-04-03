package com.hfad.digital_assistant.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        // USER INFO
        val userNameText = view.findViewById<TextView>(R.id.userNameRef)
        userNameText.text = userPreferences.getFullName()
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerRef)

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        //  VIEWMODEL
        viewModel = ViewModelProvider(
            this,
            ReflectionViewModelFactory(userPreferences)
        )[ReflectionViewModel::class.java]

        adapter = ReflectionAdapter(viewModel)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerView)
        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = null
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            isFocusable = false
            isFocusableInTouchMode = false
            setHasFixedSize(true)
            adapter = this@ReflectionFragment.adapter
        }

        //  LIVE DATA
        viewModel.questions.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        //  BUTTONS
        view.findViewById<Button>(R.id.btnSend).setOnClickListener {
            viewModel.sendAnswers()
        }

        view.findViewById<Button>(R.id.btnToday).setOnClickListener {
            viewModel.loadQuestions()
        }

        view.findViewById<Button>(R.id.btnPickDate).setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, y, m, d ->
                    val date = "$y-${m + 1}-$d"
                    // TODO: сделать запрос истории по дате
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        //  INITIAL LOAD
        viewModel.loadQuestions()
    }
}