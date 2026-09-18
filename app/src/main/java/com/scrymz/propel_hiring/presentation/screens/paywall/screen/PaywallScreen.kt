package com.scrymz.propel_hiring.presentation.screens.paywall.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scrymz.propel_hiring.presentation.viewmodels.paywall.PaywallViewModel

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formattedNextBillingDate = viewModel.calculateNextBillingDateFormatted()

    PaywallContent(
        uiState = uiState,
        nextBillingDateFormatted = formattedNextBillingDate,
        onBackClick = onDismiss,
        onPurchaseClick = { viewModel.executeFakePurchase() },
        onDoneClick = onDismiss
    )
}
