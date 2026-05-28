package com.yourname.sra.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentTaskListBinding
import com.yourname.sra.utils.Constants
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()

    private lateinit var taskAdapter: TaskAdapter
    private var currentStatus = Constants.TaskStatus.OPEN

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabs()
        setupRecyclerView()
        setupSwipeRefresh()
        observeState()

        viewModel.loadTasks(currentStatus)
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tasks_tab_open))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tasks_tab_ongoing))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tasks_tab_completed))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentStatus = when (tab?.position) {
                    0 -> Constants.TaskStatus.OPEN
                    1 -> Constants.TaskStatus.ONGOING
                    2 -> Constants.TaskStatus.COMPLETED
                    else -> Constants.TaskStatus.OPEN
                }
                viewModel.loadTasks(currentStatus)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                viewModel.loadTasks(currentStatus)
            }
        })
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onAcceptClick = { task ->
                viewModel.acceptTask(task.id)
            },
            onViewClick = { task ->
                val action = TaskListFragmentDirections
                    .actionTaskListFragmentToTaskDetailFragment(task.id)
                findNavController().navigate(action)
            }
        )
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadTasks(currentStatus)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.tasksState.collect { state ->
                        binding.swipeRefresh.isRefreshing = false
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBar.show()
                                binding.rvTasks.hide()
                                binding.emptyLayout.hide()
                                binding.errorLayout.hide()
                            }
                            is UiState.Success -> {
                                binding.progressBar.hide()
                                binding.rvTasks.show()
                                binding.emptyLayout.hide()
                                binding.errorLayout.hide()
                                taskAdapter.submitList(state.data)
                            }
                            is UiState.Empty -> {
                                binding.progressBar.hide()
                                binding.rvTasks.hide()
                                binding.emptyLayout.show()
                                binding.errorLayout.hide()
                                binding.tvEmptyMessage.text = when (currentStatus) {
                                    Constants.TaskStatus.OPEN -> getString(R.string.tasks_empty_open)
                                    Constants.TaskStatus.ONGOING -> getString(R.string.tasks_empty_ongoing)
                                    Constants.TaskStatus.COMPLETED -> getString(R.string.tasks_empty_completed)
                                    else -> getString(R.string.empty_state_message)
                                }
                            }
                            is UiState.Error -> {
                                binding.progressBar.hide()
                                binding.rvTasks.hide()
                                binding.emptyLayout.hide()
                                binding.errorLayout.show()
                                binding.tvError.text = state.message
                            }
                        }
                    }
                }

                launch {
                    viewModel.isCached.collect { cached ->
                        if (cached) {
                            binding.tvCachedIndicator.show()
                        } else {
                            binding.tvCachedIndicator.hide()
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
