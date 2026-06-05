package com.yourname.sra.ui.risk

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.yourname.sra.R
import com.yourname.sra.data.model.AreaRiskScore
import com.yourname.sra.databinding.FragmentRiskDashboardBinding
import com.yourname.sra.utils.UiState
import com.yourname.sra.utils.hide
import com.yourname.sra.utils.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Risk Dashboard Fragment
 * 
 * Displays area risk scores on a map with color-coded markers.
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5, 23.1, 23.2, 23.3
 */
@AndroidEntryPoint
class RiskDashboardFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRiskDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RiskDashboardViewModel by viewModels()
    
    private var googleMap: GoogleMap? = null
    private var currentRiskScores: List<AreaRiskScore> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiskDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMap()
        setupSwipeRefresh()
        observeState()
        subscribeToRealtimeChanges()

        // Load risk scores
        viewModel.loadRiskScores()
    }

    /**
     * Initialize the Google Map
     */
    private fun setupMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapView) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    /**
     * Called when Google Map is ready
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Configure map settings
        googleMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
        }
        
        // If we already have risk scores, display them
        if (currentRiskScores.isNotEmpty()) {
            displayRiskScoresOnMap(currentRiskScores)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadRiskScores()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.riskScoresState.collect { state ->
                    binding.swipeRefresh.isRefreshing = false
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.show()
                            binding.mapContainer.hide()
                            binding.emptyLayout.hide()
                            binding.errorLayout.hide()
                        }
                        is UiState.Success -> {
                            binding.progressBar.hide()
                            binding.mapContainer.show()
                            binding.emptyLayout.hide()
                            binding.errorLayout.hide()
                            // Map will be implemented in subtask 24.2
                            displayRiskScoresOnMap(state.data)
                        }
                        is UiState.Empty -> {
                            binding.progressBar.hide()
                            binding.mapContainer.hide()
                            binding.emptyLayout.show()
                            binding.errorLayout.hide()
                        }
                        is UiState.Error -> {
                            binding.progressBar.hide()
                            binding.mapContainer.hide()
                            binding.emptyLayout.hide()
                            binding.errorLayout.show()
                            binding.tvError.text = state.message
                        }
                    }
                }
            }
        }
    }

    /**
     * Subscribe to realtime risk score changes.
     * Requirement 23.1, 23.2, 23.3: Realtime updates for risk scores
     */
    private fun subscribeToRealtimeChanges() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.riskScoreChanges.collect {
                    // Realtime change detected, refresh handled by ViewModel automatically
                }
            }
        }
    }

    /**
     * Display risk scores on map with color-coded markers.
     * 
     * Requirements:
     * - 14.2: Display markers at (latitude, longitude)
     * - 14.3: Color code by risk_level (low=green, medium=yellow, high=orange, critical=red)
     * - 14.4: Display area_name, risk_score, risk_level, contributing_factors on marker tap
     * - 14.5: Handle null latitude/longitude by skipping marker
     * - 23.3: Support realtime updates
     * - 34.5: Handle null coordinates gracefully
     */
    private fun displayRiskScoresOnMap(riskScores: List<AreaRiskScore>) {
        currentRiskScores = riskScores
        
        val map = googleMap ?: run {
            Log.d(TAG, "Map not ready yet, will display when ready")
            return
        }
        
        // Clear existing markers
        map.clear()
        
        if (riskScores.isEmpty()) {
            Log.d(TAG, "No risk scores to display")
            return
        }
        
        val boundsBuilder = LatLngBounds.Builder()
        var markerCount = 0
        
        riskScores.forEach { score ->
            // Requirement 14.5 & 34.5: Skip marker if coordinates are null
            val lat = score.latitude
            val lng = score.longitude
            
            if (lat == 0.0 && lng == 0.0) {
                Log.d(TAG, "Skipping marker for ${score.areaName}: null coordinates")
                return@forEach
            }
            
            val position = LatLng(lat, lng)
            
            // Requirement 14.3: Color code by risk_level
            val markerColor = when (score.riskLevel.lowercase()) {
                "low" -> BitmapDescriptorFactory.HUE_GREEN
                "medium" -> BitmapDescriptorFactory.HUE_YELLOW
                "high" -> BitmapDescriptorFactory.HUE_ORANGE
                "critical" -> BitmapDescriptorFactory.HUE_RED
                else -> BitmapDescriptorFactory.HUE_BLUE // fallback
            }
            
            // Requirement 14.4: Display area_name, risk_score, risk_level, contributing_factors
            val title = "${score.areaName} - ${score.riskLevel.uppercase()}"
            val snippet = buildString {
                append("Risk Score: ${score.riskScore}\n")
                if (score.contributingFactors.isNotEmpty()) {
                    append("Factors: ${score.contributingFactors.joinToString(", ")}")
                }
            }
            
            // Requirement 14.2: Display markers at (latitude, longitude)
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
            )
            
            boundsBuilder.include(position)
            markerCount++
        }
        
        // Zoom to show all markers
        if (markerCount > 0) {
            try {
                val bounds = boundsBuilder.build()
                val padding = 100 // padding in pixels
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                Log.d(TAG, "Displayed $markerCount risk score markers on map")
            } catch (e: Exception) {
                Log.e(TAG, "Error adjusting camera", e)
                // Fallback: zoom to first marker
                if (riskScores.isNotEmpty()) {
                    val firstScore = riskScores.first()
                    map.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(firstScore.latitude, firstScore.longitude),
                            10f
                        )
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "RiskDashboardFragment"
    }
}
