package com.example.safekidsmobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.safekidsmobile.ui.screen.DashboardScreen
import com.example.safekidsmobile.ui.screen.DevicesScreen
import com.example.safekidsmobile.ui.screen.LoginScreen
import com.example.safekidsmobile.ui.screen.RegisterScreen
import com.example.safekidsmobile.ui.screen.MapScreen
import com.example.safekidsmobile.ui.screen.LocationHistoryScreen
import com.example.safekidsmobile.ui.screen.DeviceSettingsScreen
import com.example.safekidsmobile.ui.viewmodel.AuthViewModel
import com.example.safekidsmobile.ui.viewmodel.DeviceViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    viewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                viewModel = viewModel
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onLogout = {
                    viewModel.logout()
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToDevices = {
                    navController.navigate("devices")
                }
            )
        }

        composable("devices") {
            val deviceViewModel: DeviceViewModel = hiltViewModel()
            DevicesScreen(
                viewModel = deviceViewModel,
                onDeviceSelected = { device ->
                    navController.navigate("map/${device.id}")
                },
                onPairDevice = {
                    // Dialog handles pairing
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("map/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            val deviceViewModel: DeviceViewModel = hiltViewModel()
            MapScreen(
                deviceId = deviceId,
                viewModel = deviceViewModel,
                onNavigateToHistory = {
                    navController.navigate("location-history/$deviceId")
                },
                onNavigateToSettings = {
                    navController.navigate("settings/$deviceId")
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("location-history/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            val deviceViewModel: DeviceViewModel = hiltViewModel()
            LocationHistoryScreen(
                deviceId = deviceId,
                viewModel = deviceViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings/{deviceId}") { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: return@composable
            val deviceViewModel: DeviceViewModel = hiltViewModel()
            DeviceSettingsScreen(
                deviceId = deviceId,
                viewModel = deviceViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
