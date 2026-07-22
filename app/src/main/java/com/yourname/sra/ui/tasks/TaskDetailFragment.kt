package com.yourname.sra.ui.tasks

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.yourname.sra.data.model.TaskUpdate
import com.yourname.sra.utils.ImageUtils
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
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class TaskDetailFragment : Fragment() {

    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TaskViewModel by viewModels()
    private val args: TaskDetailFragmentArgs by navArgs()
    
    // Photo selection
    private var selectedPhotoUri: Uri? = null
    
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedPhotoUri = uri
                binding.tvSelectedPhoto.text = getString(
                    R.string.task_detail_photo_selected,
                    uri.lastPathSegment ?: "photo.jpg"
                )
                binding.tvSelectedPhoto.show()
            }
        }
    }

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

        setupPostUpdateUI()
        observeState()
        observeRealtimeChanges()
        observeTaskUpdateChanges()
        viewModel.loadTaskDetail(args.taskId)
        viewModel.loadTaskUpdates(args.taskId)
    }

    /**
     * Setup UI for posting task updates
     * Implements Requirement 24.1: Add UI for posting updates with text and optional photo
     */
    private fun setupPostUpdateUI() {
        binding.btnSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoPickerLauncher.launch(intent)
        }
        
        binding.btnPostUpdate.setOnClickListener {
            postTaskUpdate()
        }
    }
    
    /**
     * Post a task update with optional photo
     * Implements Requirements 24.1, 24.2, 24.3: Post update with text and optional photo
     */
    private fun postTaskUpdate() {
        val updateText = binding.etUpdateText.text.toString().trim()
        
        if (updateText.isEmpty()) {
            binding.root.showErrorSnackbar("Please enter update text")
            return
        }
        
        binding.btnPostUpdate.setLoadingState(true)
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var photoUrl: String? = null
                
                // Upload photo if selected
                selectedPhotoUri?.let { uri ->
                    val compressedBytes = ImageUtils.compressImage(
                        requireContext(),
                        uri
                    )
                    
                    if (compressedBytes != null) {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                        val fileName = "update_${timestamp}.jpg"
                        
                        val uploadResult = viewModel.uploadUpdatePhoto(compressedBytes, fileName)
                        uploadResult.fold(
                            onSuccess = { url -> photoUrl = url },
                            onFailure = { e ->
                                binding.root.showErrorSnackbar("Failed to upload photo: ${e.localizedMessage}")
                                binding.btnPostUpdate.setLoadingState(false)
                                return@launch
                            }
                        )
                    } else {
                        binding.root.showErrorSnackbar("Failed to compress image")
                        binding.btnPostUpdate.setLoadingState(false)
                        return@launch
                    }
                }
                
                // Create task update
                viewModel.createTaskUpdate(args.taskId, updateText, photoUrl)
                
                // Clear form
                binding.etUpdateText.setText("")
                selectedPhotoUri = null
                binding.tvSelectedPhoto.hide()
                
            } catch (e: Exception) {
                binding.root.showErrorSnackbar("Failed to post update: ${e.localizedMessage}")
                binding.btnPostUpdate.setLoadingState(false)
            }
        }
    }

    /**
     * Observe realtime task changes
     * Implements Requirement 20.1, 20.2, 20.3: Subscribe to task changes and refresh on change
     */
    private fun observeRealtimeChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskChanges.collect { action ->
                    // Refresh current task detail when any task change is detected
                    viewModel.loadTaskDetail(args.taskId)
                }
            }
        }
    }

    /**
     * Observe realtime task update changes
     * Implements Requirement 26.1, 26.2, 26.3: Subscribe to task_updates changes and refresh on change
     */
    private fun observeTaskUpdateChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskUpdateChanges.collect { action ->
                    // Refresh task updates when any task update change is detected
                    viewModel.loadTaskUpdates(args.taskId)
                }
            }
        }
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
                                binding.btnPostUpdate.setLoadingState(true)
                            }
                            is UiState.Success -> {
                                binding.btnAction.setLoadingState(false)
                                binding.btnPostUpdate.setLoadingState(false)
                                // The success message is already in state.data
                                binding.root.showSuccessSnackbar(state.data)
                                viewModel.resetActionState()
                            }
                            is UiState.Error -> {
                                binding.btnAction.setLoadingState(false)
                                binding.btnPostUpdate.setLoadingState(false)
                                binding.root.showErrorSnackbar(state.message)
                                viewModel.resetActionState()
                            }
                            is UiState.Empty -> {
                                binding.btnAction.setLoadingState(false)
                                binding.btnPostUpdate.setLoadingState(false)
                            }
                        }
                    }
                }

                launch {
                    viewModel.taskUpdatesState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {
                                binding.progressBarUpdates.show()
                                binding.tvNoUpdates.hide()
                                binding.layoutUpdates.hide()
                            }
                            is UiState.Success -> {
                                binding.progressBarUpdates.hide()
                                binding.tvNoUpdates.hide()
                                binding.layoutUpdates.show()
                                bindTaskUpdates(state.data)
                            }
                            is UiState.Error -> {
                                binding.progressBarUpdates.hide()
                                binding.tvNoUpdates.show()
                                binding.tvNoUpdates.text = state.message
                                binding.layoutUpdates.hide()
                            }
                            is UiState.Empty -> {
                                binding.progressBarUpdates.hide()
                                binding.tvNoUpdates.show()
                                binding.layoutUpdates.hide()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindTaskDetail(task: Task) {
        binding.tvTitle.text = task.title
        // Requirement 34.4: Handle empty description gracefully
        binding.tvDescription.text = task.description.ifEmpty { 
            getString(R.string.task_detail_no_description) 
        }
        binding.tvCategory.text = task.category
        // Requirement 34.2: Handle empty locationName gracefully
        binding.tvLocation.text = task.locationName.ifEmpty { 
            getString(R.string.location_not_specified) 
        }
        binding.tvEstimatedHours.text = getString(R.string.tasks_hours, task.estimatedHours)

        // Assigned Volunteer - Requirement 34.3: Display "Unassigned" when assigned_volunteer is null
        binding.tvAssignedVolunteer.text = if (task.assignedVolunteer != null) {
            task.assignedVolunteer
        } else {
            getString(R.string.task_detail_unassigned)
        }

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

        // Skills - handle empty skills list
        binding.chipGroupSkills.removeAllViews()
        if (task.requiredSkills.isNotEmpty()) {
            task.requiredSkills.forEach { skill ->
                val chip = Chip(requireContext()).apply {
                    text = skill
                    isClickable = false
                    setChipBackgroundColorResource(R.color.chip_background)
                    setTextColor(resources.getColor(R.color.chip_text, null))
                }
                binding.chipGroupSkills.addView(chip)
            }
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
                binding.cardPostUpdate.hide()
            }
            "ongoing" -> {
                binding.btnAction.show()
                binding.btnAction.text = getString(R.string.task_detail_complete)
                binding.btnAction.setOnClickListener {
                    showCompletionDialog(task.id)
                }
                binding.layoutFieldNotes.show()
                // Requirement 34.4: Display "No notes" when field_notes is null
                binding.etFieldNotes.setText(task.fieldNotes ?: getString(R.string.task_detail_no_notes))
                binding.etFieldNotes.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val notes = binding.etFieldNotes.text.toString().trim()
                        val noNotesText = getString(R.string.task_detail_no_notes)
                        if (notes.isNotEmpty() && notes != noNotesText) {
                            viewModel.updateFieldNotes(task.id, notes)
                        }
                    }
                }
                binding.layoutCompletionInfo.hide()
                binding.cardPostUpdate.show()

                // Show started_at - Requirement 34.4: Handle null timestamps
                if (!task.startedAt.isNullOrEmpty()) {
                    binding.tvStartedAt.show()
                    binding.tvStartedAtLabel.show()
                    binding.tvStartedAt.text = task.startedAt.formatDateTime()
                } else {
                    binding.tvStartedAt.hide()
                    binding.tvStartedAtLabel.hide()
                }
            }
            "completed" -> {
                binding.btnAction.hide()
                binding.layoutFieldNotes.show()
                // Requirement 34.4: Display "No notes" when field_notes is null
                binding.etFieldNotes.setText(task.fieldNotes ?: getString(R.string.task_detail_no_notes))
                binding.etFieldNotes.isEnabled = false
                binding.layoutCompletionInfo.show()
                binding.cardPostUpdate.hide()
                // Requirement 34.4: Display "No notes" when completion_note is null
                binding.tvCompletionNote.text = task.completionNote ?: getString(R.string.task_detail_no_notes)

                // Requirement 34.4: Handle null timestamps
                if (!task.startedAt.isNullOrEmpty()) {
                    binding.tvStartedAt.show()
                    binding.tvStartedAtLabel.show()
                    binding.tvStartedAt.text = task.startedAt.formatDateTime()
                } else {
                    binding.tvStartedAt.hide()
                    binding.tvStartedAtLabel.hide()
                }
                
                if (!task.completedAt.isNullOrEmpty()) {
                    binding.tvCompletedAt.show()
                    binding.tvCompletedAtLabel.show()
                    binding.tvCompletedAt.text = task.completedAt.formatDateTime()
                } else {
                    binding.tvCompletedAt.hide()
                    binding.tvCompletedAtLabel.hide()
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

    /**
     * Bind task updates to the updates list
     * Implements Requirement 25.2, 25.3: Display task updates ordered by created_at DESC
     */
    private fun bindTaskUpdates(updates: List<TaskUpdate>) {
        binding.layoutUpdates.removeAllViews()
        
        updates.forEach { update ->
            val updateView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_task_update, binding.layoutUpdates, false)
            
            updateView.findViewById<TextView>(R.id.tvUpdateText).text = update.updateText
            updateView.findViewById<TextView>(R.id.tvUpdateVolunteer).text = 
                getString(R.string.task_detail_update_by, update.volunteerId)
            updateView.findViewById<TextView>(R.id.tvUpdateTime).text = 
                update.createdAt.formatDateTime()
            updateView.findViewById<TextView>(R.id.tvUpdateStatus).text = 
                getStatusDisplay(update.status)
            
            binding.layoutUpdates.addView(updateView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
