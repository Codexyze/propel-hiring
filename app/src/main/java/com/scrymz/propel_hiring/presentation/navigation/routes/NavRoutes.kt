package com.scrymz.propel_hiring.presentation.navigation.routes

import kotlinx.serialization.Serializable

/**
 * Route for the Kitchen List Screen.
 */
@Serializable
object KitchenListRoute

/**
 * Type-safe Route for Kitchen Detail Screen accepting kitchenId argument.
 */
@Serializable
data class KitchenDetailRoute(val kitchenId: String)

/**
 * Route for Paywall Screen.
 */
@Serializable
object PaywallRoute
