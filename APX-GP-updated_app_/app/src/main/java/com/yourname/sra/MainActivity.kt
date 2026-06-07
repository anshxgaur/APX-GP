package com.yourname.sra

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.yourname.sra.databinding.ActivityMainBinding
import com.yourname.sra.ui.auth.AuthViewModel
import com.yourname.sra.ui.dashboard.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main activity hosting the navigation graph and managing global session state.
 * 
 * Session Expiration Handling (Requirement 3.4):
 * Observes session status changes and redirects to login when session expires
 * during app usage. Uses a flag to distinguish between:
 * - App startup (handled by SplashFragment)
 * - Session expiration during usage (handled here)
 * 
 * Notification Badge Handling (Requirement 13.4):
 * Observes unread notification count from DashboardViewModel and displays
 * badge on notification icon in bottom navigation.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val authViewModel: AuthViewModel by viewModels()
    private val dashboardViewModel: DashboardViewModel by viewModels()
    private var lastSessionActive = true // Track session state to detect expiration
    private var notificationBadge: BadgeDrawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupNotificationBadge()
        observeSessionStatus()
        observeUnreadNotifications()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation
        binding.bottomNav.setupWithNavController(navController)

        // Show/hide bottom nav based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment,
                R.id.loginFragment,
                R.id.signupFragment,
                R.id.taskDetailFragment -> {
                    binding.bottomNav.visibility = View.GONE
                }
                else -> {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * Initializes notification badge on bottom navigation (Requirement 13.4)
     */
    private fun setupNotificationBadge() {
        notificationBadge = binding.bottomNav.getOrCreateBadge(R.id.notificationFragment)
        notificationBadge?.isVisible = false
        notificationBadge?.backgroundColor = getColor(R.color.error)
    }

    /**
     * Observes unread notification count and updates badge (Requirement 13.4)
     */
    private fun observeUnreadNotifications() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                dashboardViewModel.dashboardState.collect { state ->
                    if (state is com.yourname.sra.utils.UiState.Success) {
                        val unreadCount = state.data.unreadNotificationCount
                        updateNotificationBadge(unreadCount)
                    }
                }
            }
        }
    }

    /**
     * Updates notification badge visibility and count (Requirement 13.4)
     */
    private fun updateNotificationBadge(count: Int) {
        notificationBadge?.let { badge ->
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
            } else {
                badge.isVisible = false
            }
        }
    }

    private fun observeSessionStatus() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.isSessionActive.collect { isActive ->
                    // Handle session expiration during app usage (Requirement 3.4)
                    // Only redirect if session becomes inactive from active state
                    // Avoid redirecting when app first starts (handled by SplashFragment)
                    if (!isActive && lastSessionActive) {
                        val currentDestination = navController.currentDestination?.id
                        // Only redirect if user is not already on auth screens
                        if (currentDestination != R.id.loginFragment &&
                            currentDestination != R.id.signupFragment &&
                            currentDestination != R.id.splashFragment) {
                            // Session expired - redirect to login and clear back stack
                            val navOptions = NavOptions.Builder()
                                .setPopUpTo(R.id.nav_graph, true)
                                .setLaunchSingleTop(true)
                                .build()
                            navController.navigate(R.id.loginFragment, null, navOptions)
                        }
                    }
                    lastSessionActive = isActive
                }
            }
        }
    }
}