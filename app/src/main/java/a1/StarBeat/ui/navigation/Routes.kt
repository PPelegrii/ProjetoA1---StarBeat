package a1.StarBeat.ui.navigation

/**
 * Define as rotas de navegação da aplicação.
 */
object AppRoutes {
    const val LIBRARY = "library"
    // Define a rota do jogo com um argumento obrigatório "songId"
    const val GAME = "game/{songId}"
    const val ADDMUSIC = "addmusic"
    const val PROFILE = "profile/{userId}"
    const val LOGIN = "login"

    // Função helper para navegar para o jogo com o ID
    fun gameRoute(songId: String) = "game/$songId"
}
sealed class Screen(val route: String) {
    // Rotas da Bottom Navigation Bar
    data object Library : Screen("library")
    data object Addmusic : Screen("addmusic")
    data object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: Int) = "profile/$userId"
    }

    data object SongDetail : Screen("songdetail/{songId}") {
        fun createRoute(songId: Long) = "songdetail/$songId"
    }

    data object EditSongDialogRoute : Screen("edit_song_dialog")
}
