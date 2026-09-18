package com.scrymz.propel_hiring.presentation.screens.kitchen.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrymz.propel_hiring.presentation.viewmodels.kitchen.KitchenListViewModel

@Composable
fun KitchenListScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPaywall: () -> Unit,
    viewModel: KitchenListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToPaywallEvent.collect {
            onNavigateToPaywall()
        }
    }

    KitchenListContent(
        uiState = uiState,
        onKitchenClick = onNavigateToDetail,
        onRetryClick = { viewModel.loadKitchens() }
    )
}
