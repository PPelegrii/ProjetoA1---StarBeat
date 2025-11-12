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

/**
 * Define o NavHost (gráfico de navegação) da aplicação.
 * Recebe o navController e a factory da MainActivity.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.LIBRARY,
        modifier = modifier
    ) {

        // --- Rota 1: Tela da Biblioteca ---
        composable(route = AppRoutes.LIBRARY) {
            // 1. Instancia o LibraryViewModel usando a factory
            val viewModel: LibraryViewModel = viewModel(factory = factory)

            // 2. Chama a LibraryScreen
            LibraryScreen(
                viewModel = viewModel,
                onSongSelected = { songId ->
                    // 3. Define a ação de clique: navegar para a rota do jogo
                    navController.navigate(AppRoutes.gameRoute(songId))
                }
            )
        }

        // --- Rota 2: Tela do Jogo (com argumento) ---
        composable(
            route = AppRoutes.GAME,
            arguments = listOf(
                // 1. Define qual argumento esta rota espera
                navArgument("songId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // 2. Extrai o argumento da rota
            val songId = backStackEntry.arguments?.getString("songId")

            if (songId == null) {
                // Se o ID for nulo (erro), apenas volte
                navController.popBackStack()
            } else {
                // 3. Instancia o GameViewModel usando a factory
                val viewModel: GameViewModel = viewModel(factory = factory)

                // 4. Chama a GameScreen
                GameScreen(
                    songId = songId,
                    viewModel = viewModel,
                    onGameEnd = {
                        // 5. Define a ação de fim de jogo: voltar para a biblioteca
                        navController.popBackStack(
                            route = AppRoutes.LIBRARY,
                            inclusive = false
                        )
                    }
                )
            }
        }
        // --- Rota 3: Tela AddMusic ---
        composable(route = AppRoutes.ADDMUSIC) {
            val viewModel: AddMusicViewModel = viewModel(factory = factory)

            AddMusicScreen(
                viewModel = viewModel
            )
        }

        // --- Rota 4: Tela Profile ---
        composable(route = AppRoutes.PROFILE) {
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