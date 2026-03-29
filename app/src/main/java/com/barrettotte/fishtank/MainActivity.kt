package com.barrettotte.fishtank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween

import com.barrettotte.fishtank.util.Logger
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.barrettotte.fishtank.data.api.ApiClient
import com.barrettotte.fishtank.data.repository.AuthRepository
import com.barrettotte.fishtank.data.repository.PreferencesRepository
import com.barrettotte.fishtank.data.repository.ProfileRepository
import com.barrettotte.fishtank.data.repository.StreamRepository
import com.barrettotte.fishtank.ui.grid.GridScreen
import com.barrettotte.fishtank.ui.grid.GridViewModel
import com.barrettotte.fishtank.ui.login.LoginScreen
import com.barrettotte.fishtank.ui.login.LoginViewModel
import com.barrettotte.fishtank.ui.player.PlayerScreen
import com.barrettotte.fishtank.ui.player.PlayerViewModel
import com.barrettotte.fishtank.ui.theme.FishtankTheme

/** Main entry point for the Fishtank Android TV app. */
class MainActivity : ComponentActivity() {

    /** Callback for intercepting key events at the Activity level. Set by PlayerScreen. */
    var keyEventInterceptor: ((android.view.KeyEvent) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Fishtank)
        super.onCreate(savedInstanceState)
        setContent {
            FishtankTheme {
                FishtankNavHost()
            }
        }
    }

    @Suppress("RestrictedApi")
    override fun dispatchKeyEvent(event: android.view.KeyEvent): Boolean {
        if (event.action == android.view.KeyEvent.ACTION_DOWN) {
            Logger.d("Keys", "keyCode=${event.keyCode} interceptor=${keyEventInterceptor != null}")
        }
        if (keyEventInterceptor?.invoke(event) == true) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }
}

/** Top-level navigation host with routes for login, grid, and player screens. */
@Composable
fun FishtankNavHost() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Create dependencies
    val preferencesRepository = remember { PreferencesRepository(context) }
    val api = remember { ApiClient.create(preferencesRepository) }
    val profileRepository = remember { ProfileRepository(api) }
    val authRepository = remember { AuthRepository(api, preferencesRepository, profileRepository) }
    val streamRepository = remember { StreamRepository(api) }

    // Read auto-login credentials from build config (set via .env in debug builds)
    val autoLoginEmail = BuildConfig.FT_EMAIL
    val autoLoginPassword = BuildConfig.FT_PASSWORD

    NavHost(
        navController = navController,
        startDestination = "login",
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) },
    ) {
        composable("login") {
            val viewModel = remember {
                LoginViewModel(authRepository, autoLoginEmail, autoLoginPassword)
            }
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("grid") {
                        popUpTo("login") { inclusive = true }
                    }
                },
            )
        }

        composable("grid") {
            val viewModel = remember {
                GridViewModel(streamRepository, authRepository, preferencesRepository)
            }
            GridScreen(
                viewModel = viewModel,
                onCameraSelected = { streamId ->
                    navController.navigate("player/$streamId")
                },
                onLogout = {
                    CoroutineScope(Dispatchers.Main).launch {
                        authRepository.logout()
                        navController.navigate("login") {
                            popUpTo("grid") { inclusive = true }
                        }
                    }
                },
            )
        }

        composable(
            route = "player/{streamId}",
            arguments = listOf(navArgument("streamId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getString("streamId") ?: ""
            val viewModel = remember(streamId) {
                PlayerViewModel(streamRepository, preferencesRepository, authRepository, streamId)
            }
            PlayerScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
