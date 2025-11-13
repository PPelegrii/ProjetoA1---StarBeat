package a1.StarBeat.ui.navigation

object AppRoutes {
    const val LIBRARY = "library"
    const val GAME = "game/{songId}"
    const val ADDMUSIC = "addmusic"
    const val PROFILE = "profile/{userId}"
    const val LOGIN = "login"

    fun gameRoute(songId: String) = "game/$songId"
}
sealed class Screen(val route: String) {
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
