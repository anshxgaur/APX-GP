package com.yourname.sra.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentLoginBinding
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.isValidEmail
import com.yourname.sra.utils.setLoadingState
import com.yourname.sra.utils.showErrorSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.login(email, password)
            }
        }

        binding.tvSignUpLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signupFragment)
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!email.isValidEmail()) {
            binding.tilEmail.error = getString(R.string.login_error_email)
            return false
        } else {
            binding.tilEmail.error = null
        }

        if (password.length < 6) {
            binding.tilPassword.error = getString(R.string.login_error_password)
            return false
        } else {
            binding.tilPassword.error = null
        }

        return true
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.btnLogin.setLoadingState(true)
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is UiState.Success -> {
                            binding.btnLogin.setLoadingState(false)
                            binding.progressBar.visibility = View.GONE
                            viewModel.resetLoginState()
                            findNavController().navigate(
                                R.id.action_loginFragment_to_dashboardFragment
                            )
                        }
                        is UiState.Error -> {
                            binding.btnLogin.setLoadingState(false)
                            binding.progressBar.visibility = View.GONE
                            binding.root.showErrorSnackbar(state.message)
                            viewModel.resetLoginState()
                        }
                        is UiState.Empty -> {
                            binding.btnLogin.setLoadingState(false)
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
