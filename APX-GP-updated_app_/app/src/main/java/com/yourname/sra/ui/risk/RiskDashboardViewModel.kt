package com.yourname.sra.ui.risk

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourname.sra.data.model.AreaRiskScore
import com.yourname.sra.data.repository.RiskScoreRepository
import com.yourname.sra.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Risk Dashboard
 * 
 * Manages risk score data from ML Bridge and handles realtime updates.
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5, 23.1, 23.2, 23.3
 */
@HiltViewModel
class RiskDashboardViewModel @Inject constructor(
    private val riskScoreRepository: RiskScoreRepository
) : ViewModel() {

    private val _riskScoresState = MutableStateFlow<UiState<List<AreaRiskScore>>>(UiState.Loading)
    val riskScoresState: StateFlow<UiState<List<AreaRiskScore>>> = _riskScoresState.asStateFlow()

    private val _riskScoreChanges = MutableSharedFlow<PostgresAction>()
    val riskScoreChanges: SharedFlow<PostgresAction> = _riskScoreChanges.asSharedFlow()

    init {
        // Subscribe to realtime risk_score changes
        viewModelScope.launch {
            try {
                riskScoreRepository.subscribeRiskScoreChanges().collect { action ->
                    Log.d(TAG, "Risk score change detected: $action")
                    _riskScoreChanges.emit(action)
                    // Auto-refresh risk scores on any change
                    loadRiskScores()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to risk score changes", e)
            }
        }
    }

    /**
     * Load all risk scores from database.
     * Requirement 14.1: Fetch all risk scores ordered by risk_score DESC
     */
    fun loadRiskScores() {
        viewModelScope.launch {
            _riskScoresState.value = UiState.Loading
            
            val result = riskScoreRepository.getAllRiskScores()
            result.fold(
                onSuccess = { riskScores ->
                    if (riskScores.isEmpty()) {
                        _riskScoresState.value = UiState.Empty
                        Log.d(TAG, "No risk scores found")
                    } else {
                        _riskScoresState.value = UiState.Success(riskScores)
                        Log.d(TAG, "Loaded ${riskScores.size} risk scores")
                    }
                },
                onFailure = { e ->
                    _riskScoresState.value = UiState.Error(
                        e.localizedMessage ?: "Failed to load risk scores"
                    )
                    Log.e(TAG, "Failed to load risk scores", e)
                }
            )
        }
    }

    companion object {
        private const val TAG = "RiskDashboardViewModel"
    }
}
