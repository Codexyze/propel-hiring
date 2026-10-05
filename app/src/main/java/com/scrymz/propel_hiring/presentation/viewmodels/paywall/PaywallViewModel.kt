package com.scrymz.propel_hiring.presentation.viewmodels.paywall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrymz.propel_hiring.core.analytics.Analytics
import com.scrymz.propel_hiring.domain.state.ResultState
import com.scrymz.propel_hiring.domain.usecase.subscription.ProcessSubscriptionUseCase
import com.scrymz.propel_hiring.presentation.uiStates.paywall.PaywallUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val processSubscriptionUseCase: ProcessSubscriptionUseCase,
    private val analytics: Analytics,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaywallUIState>(PaywallUIState.Idle)
    val uiState: StateFlow<PaywallUIState> = _uiState.asStateFlow()

    init {
        // Analytics Moment 3: paywall_shown
        analytics.logEvent("paywall_shown")
    }

    fun calculateNextBillingDateFormatted(): String {
        val nextBillingTimestamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        val formatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault())
        return formatter.format(Date(nextBillingTimestamp))
    }

    fun executeFakePurchase() {
        viewModelScope.launch(ioDispatcher) {
            processSubscriptionUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> _uiState.value = PaywallUIState.Processing
                    is ResultState.Success -> {
                        val nextBillingDate = calculateNextBillingDateFormatted()
                        _uiState.value = PaywallUIState.Success(nextBillingDateFormatted = nextBillingDate)
                    }
                    is ResultState.Error -> _uiState.value = PaywallUIState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = PaywallUIState.Idle
    }
}
