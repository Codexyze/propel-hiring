package com.scrymz.propel_hiring.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.scrymz.propel_hiring.domain.model.subscription.SubscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val LAUNCH_COUNT = intPreferencesKey("launch_count")
        val IS_SUBSCRIBED = booleanPreferencesKey("is_subscribed")
        val PURCHASE_TIMESTAMP = longPreferencesKey("purchase_timestamp")
    }

    val subscriptionState: Flow<SubscriptionState> = dataStore.data.map { prefs ->
        SubscriptionState(
            isSubscribed = prefs[Keys.IS_SUBSCRIBED] ?: false,
            launchCount = prefs[Keys.LAUNCH_COUNT] ?: 0,
            purchaseTimestampMillis = prefs[Keys.PURCHASE_TIMESTAMP]
        )
    }

    suspend fun incrementLaunchCount(): Int {
        var newCount = 1
        dataStore.edit { prefs ->
            val current = prefs[Keys.LAUNCH_COUNT] ?: 0
            newCount = current + 1
            prefs[Keys.LAUNCH_COUNT] = newCount
        }
        return newCount
    }

    suspend fun setSubscribed(timestampMillis: Long) {
        dataStore.edit { prefs ->
            prefs[Keys.IS_SUBSCRIBED] = true
            prefs[Keys.PURCHASE_TIMESTAMP] = timestampMillis
        }
    }
}
