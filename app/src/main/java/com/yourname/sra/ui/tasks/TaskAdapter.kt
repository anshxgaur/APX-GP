package com.yourname.sra.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.yourname.sra.R
import com.yourname.sra.data.model.Task
import com.yourname.sra.databinding.ItemTaskBinding
import com.yourname.sra.utils.getUrgencyColorRes
import com.yourname.sra.utils.getUrgencyLabel

class TaskAdapter(
    private val onAcceptClick: (Task) -> Unit,
    private val onViewClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTitle.text = task.title
            binding.tvCategory.text = task.category
            // Requirement 34.2: Handle empty locationName gracefully
            binding.tvLocation.text = task.locationName.ifEmpty { 
                binding.root.context.getString(R.string.location_not_specified) 
            }
            binding.tvEstimatedHours.text = binding.root.context.getString(
                R.string.tasks_hours, task.estimatedHours
            )

            // Urgency indicator
            val urgencyColor = ContextCompat.getColor(
                binding.root.context, getUrgencyColorRes(task.urgency)
            )
            binding.viewUrgencyDot.background.setTint(urgencyColor)
            binding.tvUrgencyLabel.text = getUrgencyLabel(task.urgency)

            // Skills chips - handle empty required skills
            binding.chipGroupSkills.removeAllViews()
            task.requiredSkills.take(3).forEach { skill ->
                val chip = Chip(binding.root.context).apply {
                    text = skill
                    isClickable = false
                    textSize = 11f
                    setChipBackgroundColorResource(R.color.chip_background)
                    setTextColor(resources.getColor(R.color.chip_text, null))
                    chipMinHeight = 28f
                }
                binding.chipGroupSkills.addView(chip)
            }

            // Action button based on status
            when (task.status) {
                "open" -> {
                    binding.btnAction.text = binding.root.context.getString(R.string.tasks_accept)
                    binding.btnAction.setOnClickListener { onAcceptClick(task) }
                }
                "ongoing" -> {
                    binding.btnAction.text = binding.root.context.getString(R.string.tasks_view_details)
                    binding.btnAction.setOnClickListener { onViewClick(task) }
                }
                "completed" -> {
                    binding.btnAction.text = binding.root.context.getString(R.string.tasks_view_summary)
                    binding.btnAction.setOnClickListener { onViewClick(task) }
                }
            }

            // Card click
            binding.root.setOnClickListener { onViewClick(task) }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
