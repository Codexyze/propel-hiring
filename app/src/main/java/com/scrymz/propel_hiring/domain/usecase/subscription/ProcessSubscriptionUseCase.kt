package com.scrymz.propel_hiring.domain.usecase.subscription

import com.scrymz.propel_hiring.core.analytics.Analytics
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ProcessSubscriptionUseCase @Inject constructor(
    private val repository: SubscriptionRepository,
    private val analytics: Analytics
) {
    suspend operator fun invoke(): Flow<ResultState<Boolean>> {
        return repository.processFakePurchase().onEach { result ->
            if (result is ResultState.Success && result.data) {
                // Analytics Moment 4: subscription_purchased
                analytics.logEvent("subscription_purchased", mapOf("amount_charged" to 1.0, "currency" to "INR"))
            }
        }
    }
}
