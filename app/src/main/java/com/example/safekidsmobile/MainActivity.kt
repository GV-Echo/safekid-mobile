package com.example.safekidsmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.safekidsmobile.ui.navigation.AppNavHost
import com.example.safekidsmobile.ui.theme.SafeKidsMobileTheme
import com.example.safekidsmobile.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SafeKidsMobileTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    val startDestination = if (authViewModel.isLoggedIn()) {
        "dashboard"
    } else {
        "login"
    }

    AppNavHost(
        navController = navController,
        startDestination = startDestination,
        viewModel = authViewModel
    )
}