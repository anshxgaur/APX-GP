package com.yourname.sra.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentNotificationBinding
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeState()

        viewModel.loadNotifications()
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter { notification ->
            if (!notification.isRead) {
                viewModel.markAsRead(notification.id)
            }
        }
        binding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NotificationFragment.adapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadNotifications()
        }
    }

    private fun observeState() {
        // Observe notification state changes
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationsState.collect { state ->
                    binding.swipeRefresh.isRefreshing = false
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.show()
                            binding.rvNotifications.hide()
                            binding.emptyLayout.hide()
                            binding.errorLayout.hide()
                        }
                        is UiState.Success -> {
                            binding.progressBar.hide()
                            binding.rvNotifications.show()
                            binding.emptyLayout.hide()
                            binding.errorLayout.hide()
                            adapter.submitList(state.data)
                        }
                        is UiState.Empty -> {
                            binding.progressBar.hide()
                            binding.rvNotifications.hide()
                            binding.emptyLayout.show()
                            binding.errorLayout.hide()
                        }
                        is UiState.Error -> {
                            binding.progressBar.hide()
                            binding.rvNotifications.hide()
                            binding.emptyLayout.hide()
                            binding.errorLayout.show()
                            binding.tvError.text = state.message
                        }
                    }
                }
            }
        }

        // Subscribe to realtime notification changes in lifecycle-aware manner
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationChanges.collect {
                    // Realtime change detected, refresh handled by ViewModel automatically
                    // This ensures the UI stays in sync with database changes
                }
            }
        }

        // Setup retry button
        binding.btnRetry.setOnClickListener {
            viewModel.loadNotifications()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
