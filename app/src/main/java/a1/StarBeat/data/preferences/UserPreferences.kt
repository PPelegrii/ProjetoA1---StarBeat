package a1.StarBeat.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Cria a instância do DataStore (nível de App)
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Gerencia a sessão do usuário (ID logado) usando DataStore.
 */
class UserPreferences(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val LOGGED_IN_USER_ID = intPreferencesKey("logged_in_user_id")
    }

    // Expõe o ID do usuário logado como um Flow
    val loggedInUserId: Flow<Int?> = dataStore.data
        .map { preferences ->
            preferences[Keys.LOGGED_IN_USER_ID]
        }

    // Salva o ID do usuário no login
    suspend fun saveUserId(userId: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.LOGGED_IN_USER_ID] = userId
        }
    }

    // Limpa o ID no logout
    suspend fun clearUserId() {
        dataStore.edit { preferences ->
            preferences.remove(Keys.LOGGED_IN_USER_ID)
        }
    }
}