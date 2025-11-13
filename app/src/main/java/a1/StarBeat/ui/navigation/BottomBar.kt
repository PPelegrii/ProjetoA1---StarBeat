package a1.StarBeat.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomBar(
    navController: NavHostController,
    currentUserId: Int? = null,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Library,
        Screen.Addmusic,
        Screen.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(modifier = modifier) {
        items.forEach { screen ->
            val screenRoutePrefix = screen.route.substringBefore("/")
            val selected = currentRoute?.startsWith(screenRoutePrefix) == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    val targetRoute = when (screen) {
                        is Screen.Profile -> {
                            val id = currentUserId ?: 0
                            screen.createRoute(id)
                        }
                        else -> screen.route
                    }
                    navController.navigate(targetRoute) {
                        launchSingleTop = true
                        restoreState = true

                        navController.graph.startDestinationRoute?.let { startRoute ->
                            popUpTo(startRoute) { saveState = true }
                        }
                    }
                },
                icon = {
                    when (screen) {
                        is Screen.Library -> Icon(Icons.Default.List, contentDescription = "Library")
                        is Screen.Addmusic -> Icon(Icons.Default.Add, contentDescription = "Add music")
                        is Screen.Profile -> Icon(Icons.Default.Person, contentDescription = "Profile")
                        else -> Icon(Icons.Default.List, contentDescription = null)
                    }
                },
                label = {
                    Text(
                        text = when (screen) {
                            is Screen.Library -> "Biblioteca"
                            is Screen.Addmusic -> "Adicionar"
                            is Screen.Profile -> "Perfil"
                            else -> ""
                        }
                    )
                }
            )
        }
    }
}

