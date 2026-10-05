package com.scrymz.propel_hiring.presentation.viewmodels.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrymz.propel_hiring.core.analytics.Analytics
import com.scrymz.propel_hiring.domain.state.ResultState
import com.scrymz.propel_hiring.domain.usecase.kitchen.GetKitchenDetailUseCase
import com.scrymz.propel_hiring.domain.usecase.subscription.GetSubscriptionStateUseCase
import com.scrymz.propel_hiring.presentation.uiStates.kitchen.KitchenDetailUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenDetailViewModel @Inject constructor(
    private val getKitchenDetailUseCase: GetKitchenDetailUseCase,
    private val getSubscriptionStateUseCase: GetSubscriptionStateUseCase,
    private val analytics: Analytics,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<KitchenDetailUIState>(KitchenDetailUIState.Loading)
    val uiState: StateFlow<KitchenDetailUIState> = _uiState.asStateFlow()

    fun loadKitchenDetail(kitchenId: String) {
        viewModelScope.launch(ioDispatcher) {
            // Analytics Moment 2: kitchen_detail_viewed
            analytics.logEvent("kitchen_detail_viewed", mapOf("kitchen_id" to kitchenId))

            val subState = getSubscriptionStateUseCase().first()

            getKitchenDetailUseCase(kitchenId).collect { result ->
                when (result) {
                    is ResultState.Loading -> _uiState.value = KitchenDetailUIState.Loading
                    is ResultState.Success -> {
                        _uiState.value = KitchenDetailUIState.Success(
                            kitchen = result.data,
                            isSubscribed = subState.isSubscribed
                        )
                    }
                    is ResultState.Error -> _uiState.value = KitchenDetailUIState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = KitchenDetailUIState.Loading
    }
}
