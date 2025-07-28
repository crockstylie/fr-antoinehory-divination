package fr.antoinehory.divination.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey // Pour stocker l'enum en String
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "interaction_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_INTERACTION_MODE = stringPreferencesKey("active_interaction_mode")
    }

    val interactionPreferencesFlow: Flow<InteractionPreferences> = context.dataStore.data
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] ?: InteractionMode.TAP.name
            InteractionPreferences(InteractionMode.valueOf(modeString))
        }.distinctUntilChanged()

    suspend fun updateActiveInteractionMode(mode: InteractionMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] = mode.name
        }
    }
}
