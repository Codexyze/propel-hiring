package com.scrymz.propel_hiring.presentation.navigation.navHost

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.scrymz.propel_hiring.presentation.navigation.routes.KitchenDetailRoute
import com.scrymz.propel_hiring.presentation.navigation.routes.KitchenListRoute
import com.scrymz.propel_hiring.presentation.navigation.routes.PaywallRoute
import com.scrymz.propel_hiring.presentation.screens.kitchen.screen.KitchenDetailScreen
import com.scrymz.propel_hiring.presentation.screens.kitchen.screen.KitchenListScreen
import com.scrymz.propel_hiring.presentation.screens.paywall.screen.PaywallScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = KitchenListRoute
    ) {
        composable<KitchenListRoute> {
            KitchenListScreen(
                onNavigateToDetail = { kitchenId ->
                    navController.navigate(KitchenDetailRoute(kitchenId = kitchenId))
                },
                onNavigateToPaywall = {
                    navController.navigate(PaywallRoute)
                }
            )
        }

        composable<KitchenDetailRoute> { backStackEntry ->
            val route: KitchenDetailRoute = backStackEntry.toRoute()
            KitchenDetailScreen(
                kitchenId = route.kitchenId,
                onBackClick = { navController.popBackStack() },
                onNavigateToPaywall = { navController.navigate(PaywallRoute) }
            )
        }

        composable<PaywallRoute> {
            PaywallScreen(
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}
