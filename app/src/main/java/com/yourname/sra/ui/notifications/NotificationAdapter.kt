package com.yourname.sra.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yourname.sra.R
import com.yourname.sra.data.model.AppNotification
import com.yourname.sra.databinding.ItemNotificationBinding
import com.yourname.sra.utils.formatDateTime

class NotificationAdapter(
    private val onItemClick: (AppNotification) -> Unit
) : ListAdapter<AppNotification, NotificationAdapter.NotificationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: AppNotification) {
            binding.tvTitle.text = notification.title
            binding.tvMessage.text = notification.message
            binding.tvTime.text = notification.createdAt.formatDateTime()
            binding.tvType.text = notification.type.replaceFirstChar { it.uppercase() }

            // Highlight unread
            val bgColor = if (!notification.isRead) {
                ContextCompat.getColor(binding.root.context, R.color.notification_unread)
            } else {
                ContextCompat.getColor(binding.root.context, R.color.notification_read)
            }
            binding.cardNotification.setCardBackgroundColor(bgColor)

            // Unread dot
            binding.viewUnreadDot.visibility =
                if (!notification.isRead) android.view.View.VISIBLE else android.view.View.GONE

            binding.root.setOnClickListener {
                onItemClick(notification)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AppNotification>() {
        override fun areItemsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppNotification, newItem: AppNotification): Boolean {
            return oldItem == newItem
        }
    }
}
