package com.scrymz.propel_hiring.presentation.uiStates.kitchen

import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen

/**
 * Dedicated sealed UI State for Kitchen Detail Screen.
 */
sealed class KitchenDetailUIState {
    object Loading : KitchenDetailUIState()
    data class Success(val kitchen: Kitchen, val isSubscribed: Boolean) : KitchenDetailUIState()
    data class Error(val message: String) : KitchenDetailUIState()
}
