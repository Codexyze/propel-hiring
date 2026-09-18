package com.scrymz.propel_hiring.domain.model.subscription

/**
 * Domain model representing application subscription and launch state.
 */
data class SubscriptionState(
    val isSubscribed: Boolean,
    val launchCount: Int,
    val purchaseTimestampMillis: Long? = null
)
