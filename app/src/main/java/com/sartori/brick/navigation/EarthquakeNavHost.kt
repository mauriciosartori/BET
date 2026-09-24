package com.sartori.brick.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sartori.brick.feature.earthquakedetail.EarthquakeDetailScreen
import com.sartori.brick.feature.earthquakelist.EarthquakeListScreen
import com.sartori.brick.feature.earthquakelist.EarthquakeListViewModel
import com.sartori.brick.feature.earthquakemap.EarthquakeMapScreen
import kotlinx.serialization.Serializable

@Serializable
internal data object EarthquakeListRoute

@Serializable
internal data object EarthquakeMapRoute

@Serializable
internal data class EarthquakeDetailRoute(val earthquakeId: String)

@Composable
fun EarthquakeNavHost(viewModel: EarthquakeListViewModel = viewModel()) {
    val navController = rememberNavController()
    // Created above the destinations so all screens share the same feed.
    NavHost(navController = navController, startDestination = EarthquakeListRoute) {
        composable<EarthquakeListRoute> {
            EarthquakeListScreen(
                viewModel = viewModel,
                onMapClick = {
                    navController.navigate(EarthquakeMapRoute) { launchSingleTop = true }
                },
                onEarthquakeClick = { id ->
                    navController.navigate(EarthquakeDetailRoute(id)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<EarthquakeMapRoute> {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            EarthquakeMapScreen(
                uiState = state,
                onBack = { navController.popBackStack() },
                onRetry = viewModel::refresh,
                onEarthquakeClick = { id ->
                    navController.navigate(EarthquakeDetailRoute(id)) { launchSingleTop = true }
                }
            )
        }
        composable<EarthquakeDetailRoute> { entry ->
            val route = entry.toRoute<EarthquakeDetailRoute>()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            EarthquakeDetailScreen(
                earthquake = state.earthquakes.firstOrNull { it.id == route.earthquakeId },
                isLoading = state.isInitialLoading,
                isFromOfflineCache = state.isFromOfflineCache,
                onBack = { navController.popBackStack() },
                onRetry = viewModel::refresh
            )
        }
    }
}
