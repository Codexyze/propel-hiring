package com.scrymz.propel_hiring.core.analytics

import android.util.Log

/**
 * Analytics abstraction for logging product and user journey events.
 *
 * Selected 4 Key Moments:
 * 1. "app_launched": Measures user retention and tracks launch count increment.
 * 2. "kitchen_detail_viewed": Measures user engagement and interest in specific kitchen profiles.
 * 3. "paywall_shown": Measures paywall exposure (triggered via 3rd launch auto-trigger or manual Subscribe tap).
 * 4. "subscription_purchased": Tracks successful fake trial activation and subscription conversion.
 */
interface Analytics {
    /**
     * Logs a named event with optional parameters.
     *
     * @param eventName Unique identifier for the analytics event.
     * @param params Key-value pairs providing contextual metadata.
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

/**
 * [Analytics] implementation that routes events to Android Logcat.
 */
class LogcatAnalytics : Analytics {
    private companion object {
        const val TAG = "TiffinAnalytics"
    }

    override fun logEvent(eventName: String, params: Map<String, Any>) {
        val paramString = if (params.isNotEmpty()) " | Params: $params" else ""
        Log.d(TAG, "EVENT: [$eventName]$paramString")
    }
}
