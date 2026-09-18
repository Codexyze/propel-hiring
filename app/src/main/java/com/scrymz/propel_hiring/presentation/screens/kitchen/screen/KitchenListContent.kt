package com.scrymz.propel_hiring.presentation.screens.kitchen.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen
import com.scrymz.propel_hiring.presentation.screens.kitchen.components.KitchenCard
import com.scrymz.propel_hiring.presentation.uiStates.kitchen.KitchenListUIState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenListContent(
    uiState: KitchenListUIState,
    onKitchenClick: (String) -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tiffin — Kitchens Near You",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is KitchenListUIState.Idle,
                is KitchenListUIState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Finding delicious home kitchens...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                is KitchenListUIState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.kitchens,
                            key = { it.id }
                        ) { kitchen ->
                            KitchenCard(
                                kitchen = kitchen,
                                onClick = { onKitchenClick(kitchen.id) }
                            )
                        }
                    }
                }

                is KitchenListUIState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Kitchens Found",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "There are no home kitchens available near your area right now.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryClick) {
                            Text("Refresh")
                        }
                    }
                }

                is KitchenListUIState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Unable to Load Kitchens",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetryClick) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun KitchenListContentLoadingPreview() {
    KitchenListContent(
        uiState = KitchenListUIState.Loading,
        onKitchenClick = {},
        onRetryClick = {}
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun KitchenListContentSuccessPreview() {
    KitchenListContent(
        uiState = KitchenListUIState.Success(
            kitchens = listOf(
                Kitchen(
                    id = "k1",
                    name = "Mom's Desi Rasoi",
                    cuisine = "North Indian",
                    pricePerTiffin = 120.0,
                    isVeg = true,
                    rating = 4.8,
                    address = "Sector 14, Gurgaon"
                ),
                Kitchen(
                    id = "k2",
                    name = "Swaad Punjabi Tiffin",
                    cuisine = "Punjabi Dhaba Style",
                    pricePerTiffin = 140.0,
                    isVeg = false,
                    rating = 4.7,
                    address = "Model Town, Ludhiana"
                )
            )
        ),
        onKitchenClick = {},
        onRetryClick = {}
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun KitchenListContentEmptyPreview() {
    KitchenListContent(
        uiState = KitchenListUIState.Empty,
        onKitchenClick = {},
        onRetryClick = {}
    )
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun KitchenListContentErrorPreview() {
    KitchenListContent(
        uiState = KitchenListUIState.Error("Failed to parse local kitchen asset"),
        onKitchenClick = {},
        onRetryClick = {}
    )
}
