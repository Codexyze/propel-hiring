package com.scrymz.propel_hiring.presentation.uiStates.paywall

/**
 * Dedicated sealed UI State for Paywall Screen.
 */
sealed class PaywallUIState {
    object Idle : PaywallUIState()
    object Processing : PaywallUIState()
    data class Success(val nextBillingDateFormatted: String) : PaywallUIState()
    data class Error(val message: String) : PaywallUIState()
}
