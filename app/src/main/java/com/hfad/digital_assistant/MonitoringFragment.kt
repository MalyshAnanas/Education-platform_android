package com.hfad.digital_assistant

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.hfad.digital_assistant.databinding.FragmentMonitoringBinding
import com.hfad.digital_assistant.model.api.RetrofitClient
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.model.api.MonitoringRepository
import com.hfad.digital_assistant.view.ProfileBottomSheet
import com.hfad.digital_assistant.view.MonitoringHistoryAdapter
import com.hfad.digital_assistant.view.MonitoringQuestionsAdapter
import com.hfad.digital_assistant.viewModel.MonitoringViewModel
import com.hfad.digital_assistant.viewModel.MonitoringViewModelFactory

class MonitoringFragment : Fragment() {

    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MonitoringViewModel
    private val questionsAdapter = MonitoringQuestionsAdapter()
    private val historyAdapter = MonitoringHistoryAdapter()
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val view = binding.root

        userPreferences = UserPreferences(requireContext())
        binding.userNameMon.text = userPreferences.getFullName() ?: "Гость"

        val api = RetrofitClient.createMonitoringApi(userPreferences)

        val repository = MonitoringRepository(api)
        val factory = MonitoringViewModelFactory(repository)

        viewModel = ViewModelProvider(this, factory)[MonitoringViewModel::class.java]

        updateUserHeader(view)

        parentFragmentManager.setFragmentResultListener(
            "profile_updated",
            viewLifecycleOwner
        ) { _, _ ->
            updateUserHeader(view)
        }

        setupRecyclerViews()
        observeViewModel()

        binding.sendButton.setOnClickListener {
            viewModel.sendAnswers(questionsAdapter.getAnswers())
        }

        binding.historyButton.setOnClickListener {
            viewModel.loadHistory()
            binding.historyBlock.visibility =
                if (binding.historyBlock.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        viewModel.loadIndicators()
        viewModel.loadHistory()

        // Получаем пользователя из Preferences
        val fullName = userPreferences.getFullName()
        // Открытие профиля
        val userNameText = view.findViewById<TextView>(R.id.userNameMon)
        val userPhotoContainer = view.findViewById<View>(R.id.userPhotoContainerMon)

        val openProfile = {
            val bottomSheet = ProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, "ProfileBottomSheet")
        }

        userNameText.text = fullName
        userNameText.setOnClickListener { openProfile() }
        userPhotoContainer.setOnClickListener { openProfile() }

        return view
    }

    private fun setupRecyclerViews() {
        binding.questionsRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.questionsRecycler.adapter = questionsAdapter

        binding.questionsRecycler.setHasFixedSize(false)
        binding.questionsRecycler.isNestedScrollingEnabled = false

        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecycler.adapter = historyAdapter
    }

    private fun observeViewModel() {
        viewModel.indicators.observe(viewLifecycleOwner) {
            questionsAdapter.submitList(it)
        }

        viewModel.history.observe(viewLifecycleOwner) {
            historyAdapter.submitList(it)
        }

        viewModel.message.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Для обнавления шапки пользователя на странице
    private fun updateUserHeader(view: View) {
        val userNameText = view.findViewById<TextView>(R.id.userNameMon)
        val userPhoto = view.findViewById<ImageView>(R.id.userImageMon)

        val fullName = userPreferences.getFullName()
        val serverPhotoUrl = userPreferences.getServerPhotoUrl()
        val localPhotoUri = userPreferences.getPhotoUri()

        userNameText.text = fullName ?: "Гость"

        when {
            !serverPhotoUrl.isNullOrBlank() -> {
                // Если используешь Coil:
                userPhoto.load(serverPhotoUrl) {
                    placeholder(R.drawable.userphoto)
                    error(R.drawable.userphoto)
                }
            }

            !localPhotoUri.isNullOrBlank() -> {
                userPhoto.setImageURI(Uri.parse(localPhotoUri))
            }

            else -> {
                userPhoto.setImageResource(R.drawable.userphoto)
            }
        }
    }
}