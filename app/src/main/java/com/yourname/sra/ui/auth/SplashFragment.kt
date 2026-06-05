package com.yourname.sra.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.yourname.sra.databinding.FragmentSplashBinding
import com.yourname.sra.utils.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Splash screen fragment that handles initial session check and routing.
 * 
 * Session Check Flow (Requirements 3.1, 3.2, 3.3):
 * 1. Display splash screen for 1.5 seconds
 * 2. Check for existing valid session via AuthRepository.isLoggedIn()
 * 3. Navigate to Dashboard if session exists (Req 3.2)
 * 4. Navigate to Login if no session exists (Req 3.3)
 * 
 * Note: Session persistence is automatic via Supabase GoTrue.
 * Sessions are stored in SharedPreferences and restored on app restart.
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Delay 1.5 seconds then check session
        viewLifecycleOwner.lifecycleScope.launch {
            kotlinx.coroutines.delay(1500)
            viewModel.checkSession()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.sessionState.collect { state ->
                    when (state) {
                        is UiState.Success -> {
                            if (state.data) {
                                // Logged in → Dashboard
                                findNavController().navigate(
                                    R.id.action_splashFragment_to_dashboardFragment
                                )
                            } else {
                                // Not logged in → Login
                                findNavController().navigate(
                                    R.id.action_splashFragment_to_loginFragment
                                )
                            }
                        }
                        is UiState.Error -> {
                            findNavController().navigate(
                                R.id.action_splashFragment_to_loginFragment
                            )
                        }
                        else -> { /* Loading or Empty — wait */ }
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
