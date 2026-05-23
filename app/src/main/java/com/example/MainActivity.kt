package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.forecast.ForecastScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.domain.LocationTracker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appContainer = (application as WeatherApplication).container
        val locationTracker = LocationTracker(this)
        
        setContent {
            MyApplicationTheme {
                val locationPermissionsState = rememberMultiplePermissionsState(
                    listOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )

                LaunchedEffect(Unit) {
                    if (!locationPermissionsState.allPermissionsGranted) {
                        locationPermissionsState.launchMultiplePermissionRequest()
                    }
                }

                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        repository = appContainer.weatherRepository,
                        locationTracker = locationTracker,
                        preferencesManager = appContainer.preferencesManager
                    )
                )

                val searchViewModel: SearchViewModel = viewModel(
                    factory = SearchViewModel.Factory(
                        repository = appContainer.weatherRepository
                    )
                )

                WeatherApp(homeViewModel, searchViewModel, appContainer)
            }
        }
    }
}

@Composable
fun WeatherApp(
    homeViewModel: HomeViewModel,
    searchViewModel: SearchViewModel,
    appContainer: com.example.di.AppContainer
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_forecast)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "forecast" } == true,
                    onClick = {
                        navController.navigate("forecast") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_search)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "search" } == true,
                    onClick = {
                        navController.navigate("search") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = currentDestination?.hierarchy?.any { it.route == "settings" } == true,
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(homeViewModel) }
            composable("forecast") { ForecastScreen(homeViewModel) }
            composable("search") { 
                SearchScreen(
                    searchViewModel = searchViewModel,
                    homeViewModel = homeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                ) 
            }
            composable("settings") { SettingsScreen(appContainer.preferencesManager) }
        }
    }
}
