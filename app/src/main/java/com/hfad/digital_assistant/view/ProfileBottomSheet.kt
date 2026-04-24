package com.hfad.digital_assistant.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hfad.digital_assistant.R
import com.hfad.digital_assistant.model.api.AuthRepository
import com.hfad.digital_assistant.model.api.RetrofitClient
import com.hfad.digital_assistant.model.api.UserPreferences
import com.hfad.digital_assistant.viewModel.ProfileViewModel
import com.hfad.digital_assistant.viewModel.ProfileViewModelFactory

class ProfileBottomSheet : BottomSheetDialogFragment() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var authRepository: AuthRepository

    private val viewModel: ProfileViewModel by viewModels {
        ProfileViewModelFactory(authRepository, userPreferences)
    }

    private lateinit var userNameTextProfile: TextView
    private lateinit var changePhoto: TextView
    private lateinit var exit: TextView
    private lateinit var etFullName: EditText
    private lateinit var etPosition: EditText
    private lateinit var etOrganization: EditText
    private lateinit var btnSaveProfile: Button
    private lateinit var userPhotoProfile: ImageView

    private val photoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let { selectedUri ->
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        selectedUri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )

                    userPhotoProfile.setImageURI(selectedUri)

                    viewModel.uploadPhoto(
                        context = requireContext(),
                        uri = selectedUri
                    )

                    parentFragmentManager.setFragmentResult(
                        "profile_updated",
                        Bundle()
                    )

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось выбрать фото",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = UserPreferences(requireContext())
        val api = RetrofitClient.create(userPreferences)
        authRepository = AuthRepository(userPreferences, api)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.profile_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        observeViewModel()

        viewModel.loadProfile()
    }

    private fun initViews(view: View) {
        userNameTextProfile = view.findViewById(R.id.userNameTextProfile)
        changePhoto = view.findViewById(R.id.ChangePhoto)
        exit = view.findViewById(R.id.Exit)
        etFullName = view.findViewById(R.id.etFullName)
        etPosition = view.findViewById(R.id.etPosition)
        etOrganization = view.findViewById(R.id.etOrganization)
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile)
        userPhotoProfile = view.findViewById(R.id.User_photo_profile)
    }

    private fun setupListeners() {
        changePhoto.setOnClickListener {
            photoPickerLauncher.launch(arrayOf("image/*"))
        }

        btnSaveProfile.setOnClickListener {
            viewModel.saveProfile(
                fullName = etFullName.text.toString().trim(),
                position = etPosition.text.toString().trim(),
                organization = etOrganization.text.toString().trim()
            )
        }

        exit.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            userNameTextProfile.text =
                if (state.fullName.isBlank()) "Гость" else state.fullName

            if (etFullName.text.toString() != state.fullName) {
                etFullName.setText(state.fullName)
            }

            if (etPosition.text.toString() != state.position) {
                etPosition.setText(state.position)
            }

            if (etOrganization.text.toString() != state.organization) {
                etOrganization.setText(state.organization)
            }

            btnSaveProfile.isEnabled = !state.isLoading

            state.photoUri?.let { uriString ->
                try {
                    userPhotoProfile.setImageURI(Uri.parse(uriString))
                } catch (_: Exception) {
                }
            }
        }

        viewModel.message.observe(viewLifecycleOwner) { text ->
            Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
        }

        viewModel.logoutEvent.observe(viewLifecycleOwner) {
            dismiss()
            findNavController().navigate(R.id.loginFragment)
        }
    }
}