package com.barrettotte.fishtank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barrettotte.fishtank.ui.grid.GridScreen
import com.barrettotte.fishtank.ui.login.LoginScreen
import com.barrettotte.fishtank.ui.player.PlayerScreen
import com.barrettotte.fishtank.ui.theme.FishtankTheme

/** Main entry point for the Fishtank Android TV app. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FishtankTheme {
                FishtankNavHost()
            }
        }
    }
}

/** Top-level navigation host with routes for login, grid, and player screens. */
@Composable
fun FishtankNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("grid") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("grid") {
            GridScreen(
                onCameraSelected = { streamId ->
                    navController.navigate("player/$streamId")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("grid") { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = "player/{streamId}",
            arguments = listOf(navArgument("streamId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            PlayerScreen(
                streamId = streamId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
