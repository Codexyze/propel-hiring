package com.scrymz.propel_hiring.presentation.screens.kitchen.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrymz.propel_hiring.presentation.uiStates.kitchen.KitchenDetailUIState
import com.scrymz.propel_hiring.presentation.viewmodels.kitchen.KitchenDetailViewModel

@Composable
fun KitchenDetailScreen(
    kitchenId: String,
    onBackClick: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: KitchenDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(kitchenId) {
        viewModel.loadKitchenDetail(kitchenId)
    }

    KitchenDetailContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSubscribeClick = {
            val currentState = uiState
            if (currentState is KitchenDetailUIState.Success) {
                if (!currentState.isSubscribed) {
                    onNavigateToPaywall()
                }
            } else {
                onNavigateToPaywall()
            }
        },
        onRetryClick = { viewModel.loadKitchenDetail(kitchenId) }
    )
}
