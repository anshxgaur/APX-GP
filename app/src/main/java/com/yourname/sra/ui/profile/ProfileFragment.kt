package com.yourname.sra.ui.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentProfileBinding
import com.yourname.sra.data.model.Volunteer
import com.yourname.sra.utils.Constants
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.setLoadingState
import com.yourname.sra.utils.show
import com.yourname.sra.utils.showErrorSnackbar
import com.yourname.sra.utils.showSuccessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var currentVolunteer: Volunteer? = null
    private var uploadedPhotoUrl: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                Glide.with(this).load(uri).circleCrop().into(binding.ivProfilePhoto)
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val fileName = "profile_${currentVolunteer?.id ?: UUID.randomUUID()}.jpg"
                        viewModel.uploadProfilePhoto(bytes, fileName)
                    }
                } catch (e: Exception) {
                    binding.root.showErrorSnackbar("Failed to read image")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSkillChips()
        setupAvailabilityDropdown()
        setupClickListeners()
        observeState()

        viewModel.loadProfile()
    }

    private fun setupSkillChips() {
        Constants.SKILL_OPTIONS.forEach { skill ->
            val chip = Chip(requireContext()).apply {
                text = skill
                isCheckable = true
                isCheckedIconVisible = true
                setChipBackgroundColorResource(R.color.chip_background)
                setTextColor(resources.getColor(R.color.chip_text, null))
                setCheckedIconTintResource(R.color.primary)
            }
            binding.chipGroupSkills.addView(chip)
        }
    }

    private fun setupAvailabilityDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            Constants.AVAILABILITY_OPTIONS
        )
        binding.actvAvailability.setAdapter(adapter)
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.profile_logout))
                .setMessage(getString(R.string.profile_logout_confirm))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    viewModel.logout()
                }
                .setNegativeButton(getString(R.string.no), null)
                .show()
        }

        binding.btnRetry.setOnClickListener {
            viewModel.loadProfile()
        }
    }

    private fun saveProfile() {
        val volunteer = currentVolunteer ?: return
        val updatedVolunteer = volunteer.copy(
            fullName = binding.etFullName.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            area = binding.etArea.text.toString().trim(),
            skills = getSelectedSkills(),
            availability = binding.actvAvailability.text.toString().trim(),
            profilePhotoUrl = uploadedPhotoUrl ?: volunteer.profilePhotoUrl
        )
        viewModel.updateProfile(updatedVolunteer)
    }

    private fun getSelectedSkills(): List<String> {
        val skills = mutableListOf<String>()
        for (i in 0 until binding.chipGroupSkills.childCount) {
            val chip = binding.chipGroupSkills.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                skills.add(chip.text.toString())
            }
        }
        return skills
    }

    private fun bindProfile(volunteer: Volunteer) {
        currentVolunteer = volunteer
        uploadedPhotoUrl = volunteer.profilePhotoUrl

        binding.etFullName.setText(volunteer.fullName)
        binding.etEmail.setText(volunteer.email)
        binding.etPhone.setText(volunteer.phone)
        binding.etArea.setText(volunteer.area)
        binding.actvAvailability.setText(volunteer.availability, false)

        // Profile photo - Requirement 34.1: Show default avatar placeholder when profile_photo_url is null
        if (!volunteer.profilePhotoUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(volunteer.profilePhotoUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(binding.ivProfilePhoto)
        } else {
            // Display default avatar placeholder when profile_photo_url is null
            binding.ivProfilePhoto.setImageResource(R.drawable.ic_person_placeholder)
        }

        // Skills
        for (i in 0 until binding.chipGroupSkills.childCount) {
            val chip = binding.chipGroupSkills.getChildAt(i) as? Chip
            chip?.isChecked = volunteer.skills.contains(chip?.text.toString())
        }

        // Impact stats
        binding.tvTasksCompleted.text = volunteer.totalTasksCompleted.toString()
        binding.tvHoursContributed.text = volunteer.totalHours.toString()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBar.show()
                                binding.contentScroll.hide()
                                binding.errorLayout.hide()
                            }
                            is UiState.Success -> {
                                binding.progressBar.hide()
                                binding.contentScroll.show()
                                binding.errorLayout.hide()
                                bindProfile(state.data)
                            }
                            is UiState.Error -> {
                                binding.progressBar.hide()
                                binding.contentScroll.hide()
                                binding.errorLayout.show()
                                binding.tvError.text = state.message
                            }
                            is UiState.Empty -> {
                                binding.progressBar.hide()
                            }
                        }
                    }
                }

                launch {
                    viewModel.updateState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.btnSave.setLoadingState(true)
                            }
                            is UiState.Success -> {
                                binding.btnSave.setLoadingState(false)
                                binding.root.showSuccessSnackbar(
                                    getString(R.string.profile_save_success)
                                )
                                viewModel.resetUpdateState()
                            }
                            is UiState.Error -> {
                                binding.btnSave.setLoadingState(false)
                                binding.root.showErrorSnackbar(state.message)
                                viewModel.resetUpdateState()
                            }
                            is UiState.Empty -> {
                                binding.btnSave.setLoadingState(false)
                            }
                        }
                    }
                }

                launch {
                    viewModel.photoUploadState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                uploadedPhotoUrl = state.data
                            }
                            is UiState.Error -> {
                                binding.root.showErrorSnackbar(state.message)
                            }
                            else -> {}
                        }
                    }
                }

                launch {
                    viewModel.logoutState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                // Logout succeeded, navigate to login
                                findNavController().navigate(
                                    R.id.action_profileFragment_to_loginFragment
                                )
                            }
                            is UiState.Error -> {
                                // Display error message but still navigate
                                // Local session has been cleared
                                binding.root.showErrorSnackbar(state.message)
                                
                                // Navigate to login after showing error
                                // This ensures user is logged out locally even if remote logout failed
                                findNavController().navigate(
                                    R.id.action_profileFragment_to_loginFragment
                                )
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
