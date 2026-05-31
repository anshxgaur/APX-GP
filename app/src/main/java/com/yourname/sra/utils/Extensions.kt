package com.yourname.sra.utils

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.yourname.sra.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// View visibility extensions
fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.showIf(condition: Boolean) {
    visibility = if (condition) View.VISIBLE else View.GONE
}

// Snackbar helpers
fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showErrorSnackbar(message: String) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_LONG)
    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.error))
    snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackbar.show()
}

fun View.showSuccessSnackbar(message: String) {
    val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
    snackbar.setBackgroundTint(ContextCompat.getColor(context, R.color.accent))
    snackbar.setTextColor(ContextCompat.getColor(context, R.color.white))
    snackbar.show()
}

fun View.showSnackbarWithRetry(message: String, onRetry: () -> Unit) {
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE)
        .setAction(context.getString(R.string.retry)) { onRetry() }
        .show()
}

// Date formatting
fun String.formatDateTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val date = inputFormat.parse(this.substringBefore("+").substringBefore("Z"))
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun String.formatDate(): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        val date = inputFormat.parse(this.substringBefore("+").substringBefore("Z"))
        date?.let { outputFormat.format(it) } ?: this
    } catch (e: Exception) {
        this
    }
}

fun getCurrentDateFormatted(): String {
    val format = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
    return format.format(Date())
}

// Email validation
fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

// Urgency color helper
fun getUrgencyColorRes(urgency: Int): Int {
    return when (urgency) {
        5 -> R.color.urgency_critical
        4 -> R.color.urgency_high
        3 -> R.color.urgency_moderate
        2 -> R.color.urgency_low
        else -> R.color.urgency_minimal
    }
}

// Urgency label
fun getUrgencyLabel(urgency: Int): String {
    return when (urgency) {
        5 -> "Critical"
        4 -> "High"
        3 -> "Moderate"
        2 -> "Low"
        else -> "Minor"
    }
}

// Status display
fun getStatusDisplay(status: String): String {
    return status.replaceFirstChar { it.uppercase() }
}

fun getStatusColorRes(status: String): Int {
    return when (status) {
        "open" -> R.color.status_open
        "ongoing" -> R.color.status_ongoing
        "completed" -> R.color.status_completed
        "cancelled" -> R.color.status_cancelled
        "pending" -> R.color.status_pending
        "reviewed" -> R.color.status_reviewed
        "actioned" -> R.color.status_actioned
        else -> R.color.text_secondary
    }
}

// Disable/enable button during loading
fun View.setLoadingState(isLoading: Boolean) {
    isEnabled = !isLoading
    alpha = if (isLoading) 0.6f else 1.0f
}
