package a1.StarBeat

import a1.StarBeat.data.local.AppDatabase
import a1.StarBeat.data.remote.RetrofitBuilder
import a1.StarBeat.data.repository.GameRepository
import a1.StarBeat.ui.navigation.AppNavigation
import a1.StarBeat.ui.navigation.BottomBar
import a1.StarBeat.ui.ViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import a1.StarBeat.ui.theme.StarBeatTheme
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import a1.StarBeat.data.media.ExoPlayerMediaService
import a1.StarBeat.data.media.MediaService
import a1.StarBeat.data.preferences.UserPreferences
import a1.StarBeat.data.preferences.dataStore
import a1.StarBeat.ui.navigation.AppRoutes
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val jamendoService = RetrofitBuilder.buildJamendoService()
        val userPreferences = UserPreferences(applicationContext.dataStore)
        val repository = GameRepository(db, jamendoService, userPreferences)
        val mediaService: MediaService by lazy {
            ExoPlayerMediaService(applicationContext)
        }
        val factory = ViewModelFactory(repository, mediaService)

        setContent {
            StarBeatTheme {
                val navController = rememberNavController()

                // Collect current user id from repository to pass to BottomBar
                val currentUserId by repository.currentUserId.collectAsState(initial = null)

                // Observe current route to decide whether to show the BottomBar
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = when {
                    currentRoute == AppRoutes.LIBRARY -> true
                    currentRoute == AppRoutes.ADDMUSIC -> true
                    currentRoute?.startsWith("profile") == true -> true
                    else -> false
                }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) {
                            BottomBar(navController = navController, currentUserId = currentUserId)
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        factory = factory,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
