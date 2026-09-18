package com.scrymz.propel_hiring.data.repo.subscription

import android.util.Log
import com.scrymz.propel_hiring.data.local.datastore.SubscriptionDataStore
import com.scrymz.propel_hiring.domain.model.subscription.SubscriptionState
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubscriptionRepositoryImpl @Inject constructor(
    private val dataStore: SubscriptionDataStore
) : SubscriptionRepository {

    private companion object {
        const val TAG = "SubscriptionRepoImpl"
    }

    override fun getSubscriptionState(): Flow<SubscriptionState> {
        return dataStore.subscriptionState
    }

    override suspend fun incrementLaunchCount(): Flow<ResultState<Int>> = flow {
        Log.d(TAG, "incrementLaunchCount: Recording application launch")
        emit(ResultState.Loading)
        try {
            val newCount = dataStore.incrementLaunchCount()
            Log.d(TAG, "incrementLaunchCount: Updated launch count to $newCount")
            emit(ResultState.Success(newCount))
        } catch (e: Exception) {
            Log.e(TAG, "incrementLaunchCount: Error updating launch count", e)
            emit(ResultState.Error(e.message ?: "Failed to update launch count"))
        }
    }

    override suspend fun processFakePurchase(): Flow<ResultState<Boolean>> = flow {
        Log.d(TAG, "processFakePurchase: Executing fake trial activation purchase")
        emit(ResultState.Loading)
        try {
            val currentTime = System.currentTimeMillis()
            dataStore.setSubscribed(currentTime)
            Log.d(TAG, "processFakePurchase: Fake purchase successful, persisted status")
            emit(ResultState.Success(true))
        } catch (e: Exception) {
            Log.e(TAG, "processFakePurchase: Error completing fake purchase", e)
            emit(ResultState.Error(e.message ?: "Failed to complete purchase"))
        }
    }
}
