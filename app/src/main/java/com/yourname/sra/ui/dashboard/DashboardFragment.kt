package com.yourname.sra.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentDashboardBinding
import com.yourname.sra.data.model.DashboardData
import com.yourname.sra.data.model.Task
import com.yourname.sra.ui.tasks.TaskAdapter
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.getCurrentDateFormatted
import com.yourname.sra.utils.getUrgencyColorRes
import com.yourname.sra.utils.getUrgencyLabel
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.show
import com.yourname.sra.utils.showErrorSnackbar
import com.yourname.sra.utils.showSuccessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var urgentTaskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackNavigation()
        setupRecyclerView()
        setupSwipeRefresh()
        observeState()

        viewModel.loadDashboard()
    }

    /**
     * Prevents back navigation to LoginFragment (Requirement 35.4).
     * Dashboard is a root screen - back button should minimize app.
     */
    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Move app to background instead of navigating back
                    requireActivity().moveTaskToBack(true)
                }
            }
        )
    }

    private fun setupRecyclerView() {
        urgentTaskAdapter = TaskAdapter(
            onAcceptClick = { task -> viewModel.acceptTask(task) },
            onViewClick = { task ->
                val action = DashboardFragmentDirections
                    .actionDashboardFragmentToTaskDetailFragment(task.id)
                findNavController().navigate(action)
            }
        )
        binding.rvUrgentTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = urgentTaskAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadDashboard()
        }
        
        // Retry button for error state
        binding.btnRetry.setOnClickListener {
            viewModel.loadDashboard()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.dashboardState.collect { state ->
                        binding.swipeRefresh.isRefreshing = false
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBar.show()
                                binding.contentLayout.hide()
                                binding.errorLayout.hide()
                            }
                            is UiState.Success -> {
                                binding.progressBar.hide()
                                binding.contentLayout.show()
                                binding.errorLayout.hide()
                                bindDashboardData(state.data)
                            }
                            is UiState.Error -> {
                                binding.progressBar.hide()
                                binding.contentLayout.hide()
                                binding.errorLayout.show()
                                binding.tvError.text = state.message
                            }
                            is UiState.Empty -> {
                                binding.progressBar.hide()
                                binding.contentLayout.show()
                                binding.errorLayout.hide()
                            }
                        }
                    }
                }

                launch {
                    viewModel.acceptTaskState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                binding.root.showSuccessSnackbar(
                                    getString(R.string.task_detail_accept_success)
                                )
                                viewModel.resetAcceptState()
                            }
                            is UiState.Error -> {
                                binding.root.showErrorSnackbar(state.message)
                                viewModel.resetAcceptState()
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun bindDashboardData(data: DashboardData) {
        // Greeting
        binding.tvGreeting.text = getString(R.string.dashboard_greeting, data.volunteer.fullName)
        binding.tvDate.text = getCurrentDateFormatted()

        // Stats
        binding.tvTasksCompleted.text = data.volunteer.totalTasksCompleted.toString()
        binding.tvHoursContributed.text = data.volunteer.totalHours.toString()

        // Active Task - Requirement 34.3: Handle null assigned_volunteer
        if (data.ongoingTask != null) {
            binding.cardActiveTask.show()
            binding.tvNoActiveTask.hide()
            binding.tvActiveTaskTitle.text = data.ongoingTask.title
            // Requirement 34.3: Display "Not specified" when locationName is empty
            binding.tvActiveTaskLocation.text = data.ongoingTask.locationName.ifEmpty { 
                getString(R.string.location_not_specified) 
            }
            val urgencyColor = ContextCompat.getColor(
                requireContext(), getUrgencyColorRes(data.ongoingTask.urgency)
            )
            binding.viewActiveUrgency.setBackgroundColor(urgencyColor)
            binding.tvActiveUrgencyLabel.text = getUrgencyLabel(data.ongoingTask.urgency)
            binding.btnViewActiveTask.setOnClickListener {
                val action = DashboardFragmentDirections
                    .actionDashboardFragmentToTaskDetailFragment(data.ongoingTask.id)
                findNavController().navigate(action)
            }
        } else {
            binding.cardActiveTask.hide()
            binding.tvNoActiveTask.show()
        }

        // Urgent Tasks
        if (data.openTasks.isNotEmpty()) {
            binding.rvUrgentTasks.show()
            binding.tvNoUrgentTasks.hide()
            urgentTaskAdapter.submitList(data.openTasks)
        } else {
            binding.rvUrgentTasks.hide()
            binding.tvNoUrgentTasks.show()
        }

        // Recent Surveys - Requirement 34.2: Handle null photo_url by not displaying image
        if (data.recentSurveys.isNotEmpty()) {
            binding.layoutRecentSurveys.show()
            binding.tvNoRecentSurveys.hide()
            // Show up to 2 recent surveys inline
            val survey1 = data.recentSurveys.getOrNull(0)
            val survey2 = data.recentSurveys.getOrNull(1)

            if (survey1 != null) {
                binding.cardSurvey1.show()
                binding.tvSurvey1Category.text = survey1.category
                // Requirement 34.2: Handle empty locationName gracefully
                binding.tvSurvey1Location.text = survey1.locationName.ifEmpty { 
                    getString(R.string.location_not_specified) 
                }
                binding.tvSurvey1Status.text = survey1.status.replaceFirstChar { it.uppercase() }
            } else {
                binding.cardSurvey1.hide()
            }

            if (survey2 != null) {
                binding.cardSurvey2.show()
                binding.tvSurvey2Category.text = survey2.category
                // Requirement 34.2: Handle empty locationName gracefully
                binding.tvSurvey2Location.text = survey2.locationName.ifEmpty { 
                    getString(R.string.location_not_specified) 
                }
                binding.tvSurvey2Status.text = survey2.status.replaceFirstChar { it.uppercase() }
            } else {
                binding.cardSurvey2.hide()
            }
        } else {
            binding.layoutRecentSurveys.hide()
            binding.tvNoRecentSurveys.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
