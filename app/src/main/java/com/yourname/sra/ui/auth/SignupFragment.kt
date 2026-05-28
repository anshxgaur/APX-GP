package com.yourname.sra.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentSignupBinding
import com.yourname.sra.utils.Constants
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.isValidEmail
import com.yourname.sra.utils.setLoadingState
import com.yourname.sra.utils.showErrorSnackbar
import com.yourname.sra.utils.showSuccessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSkillChips()
        setupAvailabilityDropdown()
        setupClickListeners()
        observeState()
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
        binding.actvAvailability.setText(Constants.AVAILABILITY_OPTIONS[0], false)
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener {
            if (validateInputs()) {
                val fullName = binding.etFullName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                val area = binding.etArea.text.toString().trim()
                val skills = getSelectedSkills()
                val availability = binding.actvAvailability.text.toString().trim()

                viewModel.signUp(email, password, fullName, phone, area, skills, availability)
            }
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigateUp()
        }
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

    private fun validateInputs(): Boolean {
        var isValid = true

        val fullName = binding.etFullName.text.toString().trim()
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.signup_error_name)
            isValid = false
        } else {
            binding.tilFullName.error = null
        }

        val email = binding.etEmail.text.toString().trim()
        if (!email.isValidEmail()) {
            binding.tilEmail.error = getString(R.string.signup_error_email)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        val password = binding.etPassword.text.toString().trim()
        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.signup_error_password)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        if (confirmPassword != password) {
            binding.tilConfirmPassword.error = getString(R.string.signup_error_confirm)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        val area = binding.etArea.text.toString().trim()
        if (area.isEmpty()) {
            binding.tilArea.error = getString(R.string.signup_error_area)
            isValid = false
        } else {
            binding.tilArea.error = null
        }

        return isValid
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signUpState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnSignup.setLoadingState(true)
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.btnSignup.setLoadingState(false)
                            binding.progressBar.visibility = View.GONE
                            binding.root.showSuccessSnackbar(getString(R.string.signup_success))
                            viewModel.resetSignUpState()
                            findNavController().navigate(
                                R.id.action_signupFragment_to_dashboardFragment
                            )
                        }
                        is UiState.Error -> {
                            binding.btnSignup.setLoadingState(false)
                            binding.progressBar.visibility = View.GONE
                            binding.root.showErrorSnackbar(state.message)
                            viewModel.resetSignUpState()
                        }
                        is UiState.Empty -> {
                            binding.btnSignup.setLoadingState(false)
                            binding.progressBar.visibility = View.GONE
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
