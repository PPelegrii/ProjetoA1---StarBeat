package a1.StarBeat.ui

import a1.StarBeat.data.media.MediaService
import a1.StarBeat.data.repository.GameRepository
import a1.StarBeat.ui.viewmodels.AddMusicViewModel
import a1.StarBeat.ui.viewmodels.AuthViewModel
import a1.StarBeat.ui.viewmodels.GameViewModel
import a1.StarBeat.ui.viewmodels.LibraryViewModel
import a1.StarBeat.ui.viewmodels.ProfileViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory(
    private val repository: GameRepository,
    private val mediaService: MediaService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // Cria o AuthViewModel
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AuthViewModel(repository) as T
            }

            // Cria o GameViewModel
            modelClass.isAssignableFrom(GameViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                GameViewModel(repository, mediaService) as T
            }

            // Cria o LibraryViewModel
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                LibraryViewModel(repository) as T
            }

            // Cria o AddMusicViewModel
            modelClass.isAssignableFrom(AddMusicViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                AddMusicViewModel(repository) as T
            }

            // Cria o ProfileViewModel
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                ProfileViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}