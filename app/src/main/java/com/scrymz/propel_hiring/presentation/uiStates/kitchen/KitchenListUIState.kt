package com.scrymz.propel_hiring.presentation.uiStates.kitchen

import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen

/**
 * Dedicated sealed UI State for Kitchen List Screen.
 */
sealed class KitchenListUIState {
    object Idle : KitchenListUIState()
    object Loading : KitchenListUIState()
    data class Success(val kitchens: List<Kitchen>) : KitchenListUIState()
    object Empty : KitchenListUIState()
    data class Error(val message: String) : KitchenListUIState()
}
