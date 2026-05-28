package com.yourname.sra.ui.survey

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentSurveyBinding
import com.yourname.sra.utils.Constants
import com.yourname.sra.utils.LocationHelper
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
class SurveyFragment : Fragment() {

    private var _binding: FragmentSurveyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SurveyViewModel by viewModels()
    private lateinit var locationHelper: LocationHelper
    private var selectedImageUri: Uri? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            captureLocation()
        } else {
            binding.root.showErrorSnackbar(getString(R.string.location_permission_required))
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                binding.ivSurveyPhoto.show()
                Glide.with(this).load(uri).centerCrop().into(binding.ivSurveyPhoto)
                binding.btnAddPhoto.text = getString(R.string.survey_change_photo)

                // Upload photo
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val fileName = "survey_${UUID.randomUUID()}.jpg"
                        viewModel.uploadPhoto(bytes, fileName)
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
        _binding = FragmentSurveyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireContext())
        setupCategoryDropdown()
        setupSeveritySlider()
        setupClickListeners()
        observeState()
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            Constants.SURVEY_CATEGORIES
        )
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupSeveritySlider() {
        binding.sliderSeverity.addOnChangeListener { _, value, _ ->
            val label = when (value.toInt()) {
                1 -> getString(R.string.survey_severity_minor)
                2 -> getString(R.string.survey_severity_low)
                3 -> getString(R.string.survey_severity_moderate)
                4 -> getString(R.string.survey_severity_high)
                5 -> getString(R.string.survey_severity_critical)
                else -> ""
            }
            binding.tvSeverityValue.text = label
        }
        binding.sliderSeverity.value = 1f
        binding.tvSeverityValue.text = getString(R.string.survey_severity_minor)
    }

    private fun setupClickListeners() {
        binding.btnCaptureGps.setOnClickListener {
            if (locationHelper.hasLocationPermission()) {
                captureLocation()
            } else {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        binding.btnAddPhoto.setOnClickListener {
            openImagePicker()
        }

        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                val category = binding.actvCategory.text.toString().trim()
                val severity = binding.sliderSeverity.value.toInt()
                val peopleAffected = binding.etPeopleAffected.text.toString().trim().toIntOrNull() ?: 0
                val description = binding.etDescription.text.toString().trim()
                val locationName = binding.etLocationName.text.toString().trim()

                viewModel.submitSurvey(category, severity, peopleAffected, description, locationName)
            }
        }
    }

    private fun captureLocation() {
        binding.tvGpsCoordinates.text = getString(R.string.loading)
        locationHelper.getCurrentLocation(
            onSuccess = { lat, lng ->
                viewModel.setLocation(lat, lng)
                binding.tvGpsCoordinates.text = getString(R.string.survey_gps_captured, lat, lng)
            },
            onFailure = { error ->
                binding.tvGpsCoordinates.text = getString(R.string.survey_gps_placeholder)
                binding.root.showErrorSnackbar(error)
            }
        )
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.actvCategory.text.toString().trim().isEmpty()) {
            binding.tilCategory.error = getString(R.string.survey_error_category)
            isValid = false
        } else {
            binding.tilCategory.error = null
        }

        val people = binding.etPeopleAffected.text.toString().trim()
        if (people.isEmpty() || people.toIntOrNull() == null) {
            binding.tilPeopleAffected.error = getString(R.string.survey_error_people)
            isValid = false
        } else {
            binding.tilPeopleAffected.error = null
        }

        if (binding.etLocationName.text.toString().trim().isEmpty()) {
            binding.tilLocationName.error = getString(R.string.survey_error_location)
            isValid = false
        } else {
            binding.tilLocationName.error = null
        }

        if (viewModel.currentLatitude == null || viewModel.currentLongitude == null) {
            binding.root.showErrorSnackbar(getString(R.string.survey_error_gps))
            isValid = false
        }

        return isValid
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.submitState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.btnSubmit.setLoadingState(true)
                                binding.progressBar.show()
                            }
                            is UiState.Success -> {
                                binding.btnSubmit.setLoadingState(false)
                                binding.progressBar.hide()
                                binding.root.showSuccessSnackbar(getString(R.string.survey_success))
                                viewModel.resetSubmitState()
                                findNavController().navigate(R.id.dashboardFragment)
                            }
                            is UiState.Error -> {
                                binding.btnSubmit.setLoadingState(false)
                                binding.progressBar.hide()
                                binding.root.showErrorSnackbar(state.message)
                                viewModel.resetSubmitState()
                            }
                            is UiState.Empty -> {
                                binding.btnSubmit.setLoadingState(false)
                                binding.progressBar.hide()
                            }
                        }
                    }
                }

                launch {
                    viewModel.photoUploadState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.photoProgressBar.show()
                            }
                            is UiState.Success -> {
                                binding.photoProgressBar.hide()
                            }
                            is UiState.Error -> {
                                binding.photoProgressBar.hide()
                                binding.root.showErrorSnackbar(state.message)
                            }
                            is UiState.Empty -> {
                                binding.photoProgressBar.hide()
                            }
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
