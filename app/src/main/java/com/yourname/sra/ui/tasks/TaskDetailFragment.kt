package com.yourname.sra.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.yourname.sra.R
import com.yourname.sra.databinding.FragmentTaskDetailBinding
import com.yourname.sra.data.model.Task
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.formatDateTime
import com.yourname.sra.utils.getStatusColorRes
import com.yourname.sra.utils.getStatusDisplay
import com.yourname.sra.utils.getUrgencyColorRes
import com.yourname.sra.utils.getUrgencyLabel
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.setLoadingState
import com.yourname.sra.utils.show
import com.yourname.sra.utils.showErrorSnackbar
import com.yourname.sra.utils.showSuccessSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private val args: TaskDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        observeState()
        viewModel.loadTaskDetail(args.taskId)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.taskDetailState.collect { state ->
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
                                bindTaskDetail(state.data)
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
                    viewModel.actionState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.btnAction.setLoadingState(true)
                            }
                            is UiState.Success -> {
                                binding.btnAction.setLoadingState(false)
                                val message = when (state.data) {
                                    "accepted" -> getString(R.string.task_detail_accept_success)
                                    "completed" -> getString(R.string.task_detail_complete_success)
                                    else -> ""
                                }
                                if (message.isNotEmpty()) {
                                    binding.root.showSuccessSnackbar(message)
                                }
                                viewModel.resetActionState()
                            }
                            is UiState.Error -> {
                                binding.btnAction.setLoadingState(false)
                                binding.root.showErrorSnackbar(state.message)
                                viewModel.resetActionState()
                            }
                            is UiState.Empty -> {
                                binding.btnAction.setLoadingState(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindTaskDetail(task: Task) {
        binding.tvTitle.text = task.title
        binding.tvDescription.text = task.description.ifEmpty { "No description provided" }
        binding.tvCategory.text = task.category
        binding.tvLocation.text = task.locationName.ifEmpty { "Not specified" }
        binding.tvEstimatedHours.text = getString(R.string.tasks_hours, task.estimatedHours)

        // Urgency
        val urgencyColor = ContextCompat.getColor(
            requireContext(), getUrgencyColorRes(task.urgency)
        )
        binding.viewUrgencyDot.setBackgroundColor(urgencyColor)
        binding.tvUrgencyLabel.text = getUrgencyLabel(task.urgency)

        // Status
        binding.tvStatus.text = getStatusDisplay(task.status)
        binding.tvStatus.setTextColor(
            ContextCompat.getColor(requireContext(), getStatusColorRes(task.status))
        )

        // Skills
        binding.chipGroupSkills.removeAllViews()
        task.requiredSkills.forEach { skill ->
            val chip = Chip(requireContext()).apply {
                text = skill
                isClickable = false
                setChipBackgroundColorResource(R.color.chip_background)
                setTextColor(resources.getColor(R.color.chip_text, null))
            }
            binding.chipGroupSkills.addView(chip)
        }

        // Status-specific UI
        when (task.status) {
            "open" -> {
                binding.btnAction.show()
                binding.btnAction.text = getString(R.string.task_detail_accept)
                binding.btnAction.setOnClickListener {
                    showAcceptConfirmation(task.id)
                }
                binding.layoutFieldNotes.hide()
                binding.layoutCompletionInfo.hide()
            }
            "ongoing" -> {
                binding.btnAction.show()
                binding.btnAction.text = getString(R.string.task_detail_complete)
                binding.btnAction.setOnClickListener {
                    showCompletionDialog(task.id)
                }
                binding.layoutFieldNotes.show()
                binding.etFieldNotes.setText(task.fieldNotes ?: "")
                binding.etFieldNotes.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val notes = binding.etFieldNotes.text.toString().trim()
                        if (notes.isNotEmpty()) {
                            viewModel.updateFieldNotes(task.id, notes)
                        }
                    }
                }
                binding.layoutCompletionInfo.hide()

                // Show started_at
                if (!task.startedAt.isNullOrEmpty()) {
                    binding.tvStartedAt.show()
                    binding.tvStartedAtLabel.show()
                    binding.tvStartedAt.text = task.startedAt.formatDateTime()
                }
            }
            "completed" -> {
                binding.btnAction.hide()
                binding.layoutFieldNotes.show()
                binding.etFieldNotes.setText(task.fieldNotes ?: "")
                binding.etFieldNotes.isEnabled = false
                binding.layoutCompletionInfo.show()
                binding.tvCompletionNote.text = task.completionNote ?: "No note"

                if (!task.startedAt.isNullOrEmpty()) {
                    binding.tvStartedAt.show()
                    binding.tvStartedAtLabel.show()
                    binding.tvStartedAt.text = task.startedAt.formatDateTime()
                }
                if (!task.completedAt.isNullOrEmpty()) {
                    binding.tvCompletedAt.show()
                    binding.tvCompletedAtLabel.show()
                    binding.tvCompletedAt.text = task.completedAt.formatDateTime()
                }
            }
        }
    }

    private fun showAcceptConfirmation(taskId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.task_detail_accept))
            .setMessage(getString(R.string.task_detail_accept_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.acceptTask(taskId)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showCompletionDialog(taskId: String) {
        val textInputLayout = TextInputLayout(requireContext()).apply {
            hint = getString(R.string.task_detail_complete_dialog_hint)
            setPadding(48, 16, 48, 0)
        }
        val editText = TextInputEditText(requireContext())
        textInputLayout.addView(editText)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.task_detail_complete_dialog_title))
            .setView(textInputLayout)
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                val note = editText.text.toString().trim()
                if (note.isEmpty()) {
                    binding.root.showErrorSnackbar(
                        getString(R.string.task_detail_complete_error_note)
                    )
                } else {
                    viewModel.completeTask(taskId, note)
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
