package com.scrymz.propel_hiring.presentation.viewmodels.kitchen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scrymz.propel_hiring.domain.state.ResultState
import com.scrymz.propel_hiring.domain.usecase.kitchen.GetKitchensUseCase
import com.scrymz.propel_hiring.domain.usecase.subscription.GetSubscriptionStateUseCase
import com.scrymz.propel_hiring.domain.usecase.subscription.RecordAppLaunchUseCase
import com.scrymz.propel_hiring.presentation.uiStates.kitchen.KitchenListUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KitchenListViewModel @Inject constructor(
    private val getKitchensUseCase: GetKitchensUseCase,
    private val recordAppLaunchUseCase: RecordAppLaunchUseCase,
    private val getSubscriptionStateUseCase: GetSubscriptionStateUseCase,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<KitchenListUIState>(KitchenListUIState.Idle)
    val uiState: StateFlow<KitchenListUIState> = _uiState.asStateFlow()

    private val _navigateToPaywallEvent = MutableSharedFlow<Unit>()
    val navigateToPaywallEvent: SharedFlow<Unit> = _navigateToPaywallEvent.asSharedFlow()

    init {
        checkAppLaunchAndPaywallTrigger()
        loadKitchens()
    }

    private fun checkAppLaunchAndPaywallTrigger() {
        viewModelScope.launch(ioDispatcher) {
            recordAppLaunchUseCase().collect { launchResult ->
                if (launchResult is ResultState.Success) {
                    val subState = getSubscriptionStateUseCase().first()
                    if (!subState.isSubscribed && subState.launchCount == 3) {
                        _navigateToPaywallEvent.emit(Unit)
                    }
                }
            }
        }
    }

    fun loadKitchens() {
        viewModelScope.launch(ioDispatcher) {
            getKitchensUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> _uiState.value = KitchenListUIState.Loading
                    is ResultState.Success -> {
                        if (result.data.isEmpty()) {
                            _uiState.value = KitchenListUIState.Empty
                        } else {
                            _uiState.value = KitchenListUIState.Success(result.data)
                        }
                    }
                    is ResultState.Error -> _uiState.value = KitchenListUIState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = KitchenListUIState.Idle
    }
}
