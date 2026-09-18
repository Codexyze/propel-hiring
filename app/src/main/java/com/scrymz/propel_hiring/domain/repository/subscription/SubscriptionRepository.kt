package com.scrymz.propel_hiring.domain.repository.subscription

import com.scrymz.propel_hiring.domain.model.subscription.SubscriptionState
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscriptionState(): Flow<SubscriptionState>
    suspend fun incrementLaunchCount(): Flow<ResultState<Int>>
    suspend fun processFakePurchase(): Flow<ResultState<Boolean>>
}
