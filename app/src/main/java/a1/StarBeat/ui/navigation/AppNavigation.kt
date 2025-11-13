package a1.StarBeat.ui.navigation

import a1.StarBeat.ui.screens.AddMusicScreen
import a1.StarBeat.ui.screens.GameScreen
import a1.StarBeat.ui.viewmodels.GameViewModel
import a1.StarBeat.ui.viewmodels.LibraryViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import a1.StarBeat.ui.screens.LibraryScreen
import a1.StarBeat.ui.screens.ProfileScreen
import a1.StarBeat.ui.viewmodels.AddMusicViewModel
import a1.StarBeat.ui.viewmodels.ProfileViewModel
import androidx.lifecycle.ViewModelProvider
import a1.StarBeat.ui.viewmodels.AuthViewModel
import a1.StarBeat.ui.screens.LoginScreen
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AppNavigation(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    currentUserId: Int? = null,
    modifier: Modifier = Modifier
) {
    val startDestination = if (currentUserId == null) AppRoutes.LOGIN else Screen.Profile.createRoute(currentUserId)

    LaunchedEffect(currentUserId) {
        val startRoute = navController.graph.startDestinationRoute ?: AppRoutes.LIBRARY
        if (currentUserId != null) {

            val target = Screen.Profile.createRoute(currentUserId)
            if (navController.currentBackStackEntry?.destination?.route != target) {
                navController.navigate(target) {
                    launchSingleTop = true
                    popUpTo(startRoute) { inclusive = false }
                }
            }
        } else {

            if (navController.currentBackStackEntry?.destination?.route != AppRoutes.LOGIN) {
                navController.navigate(AppRoutes.LOGIN) {
                    launchSingleTop = true
                    popUpTo(startRoute) { inclusive = true }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(route = AppRoutes.LOGIN) {
            val viewModel: AuthViewModel = viewModel(factory = factory)
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        }

        composable(route = AppRoutes.LIBRARY) {
            val viewModel: LibraryViewModel = viewModel(factory = factory)

            LibraryScreen(
                viewModel = viewModel,
                onSongSelected = { songId ->
                    navController.navigate(AppRoutes.gameRoute(songId))
                }
            )
        }

        composable(
            route = AppRoutes.GAME,
            arguments = listOf(
                navArgument("songId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val songId = backStackEntry.arguments?.getString("songId")

            if (songId == null) {
                navController.popBackStack()
            } else {
                val viewModel: GameViewModel = viewModel(factory = factory)

                GameScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onGameEnd = {
                        navController.popBackStack(
                            route = AppRoutes.LIBRARY,
                            inclusive = false
                        )
                    }
                )
            }
        }
        composable(route = AppRoutes.ADDMUSIC) {
            val viewModel: AddMusicViewModel = viewModel(factory = factory)

            AddMusicScreen(
                viewModel = viewModel
            )
        }

        composable(route = AppRoutes.PROFILE) { backStackEntry ->
            val viewModel: ProfileViewModel = viewModel(factory = factory)

            ProfileScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.popBackStack(
                        route = AppRoutes.LIBRARY,
                        inclusive = false
                    )
                }
            )
        }
    }
}