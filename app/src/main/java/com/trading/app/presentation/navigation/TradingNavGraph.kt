package com.trading.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.trading.app.presentation.dashboard.DashboardScreen
import com.trading.app.presentation.orderentry.OrderEntryScreen
import com.trading.app.presentation.portfolio.PortfolioScreen
import com.trading.app.presentation.stockdetail.StockDetailScreen
import com.trading.app.presentation.watchlist.WatchlistScreen

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object Watchlist : Screen("watchlist")
    data object Portfolio : Screen("portfolio")
    data object StockDetail : Screen("stock/{symbol}") {
        fun createRoute(symbol: String) = "stock/$symbol"
    }
    data object OrderEntry : Screen("order/{symbol}") {
        fun createRoute(symbol: String) = "order/$symbol"
    }
}

@Composable
fun TradingNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStockClick = { symbol -> navController.navigate(Screen.StockDetail.createRoute(symbol)) }
            )
        }
        composable(Screen.Watchlist.route) {
            WatchlistScreen(
                onStockClick = { symbol -> navController.navigate(Screen.StockDetail.createRoute(symbol)) }
            )
        }
        composable(Screen.Portfolio.route) {
            PortfolioScreen(
                onStockClick = { symbol -> navController.navigate(Screen.StockDetail.createRoute(symbol)) }
            )
        }
        composable(
            route = Screen.StockDetail.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "trading://stock/{symbol}" })
        ) {
            StockDetailScreen(
                onBack = { navController.popBackStack() },
                onTrade = { symbol -> navController.navigate(Screen.OrderEntry.createRoute(symbol)) }
            )
        }
        composable(
            route = Screen.OrderEntry.route,
            arguments = listOf(navArgument("symbol") { type = NavType.StringType }),
            deepLinks = listOf(navDeepLink { uriPattern = "trading://order/{symbol}" })
        ) {
            OrderEntryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
