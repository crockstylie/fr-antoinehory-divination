package fr.antoinehory.divination.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.antoinehory.divination.data.model.InteractionMode // NOUVEL IMPORT
import fr.antoinehory.divination.data.model.InteractionPreferences // NOUVEL IMPORT
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val ACTIVE_INTERACTION_MODE = stringPreferencesKey("active_interaction_mode")
        val ACTIVE_DICE_SET_ID = longPreferencesKey("active_dice_set_id")
    }

    val interactionPreferencesFlow: Flow<InteractionPreferences> = context.userSettingsDataStore.data
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] ?: InteractionMode.TAP.name
            InteractionPreferences(InteractionMode.valueOf(modeString))
        }.distinctUntilChanged()

    suspend fun updateActiveInteractionMode(mode: InteractionMode) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] = mode.name
        }
    }

    val activeDiceSetIdFlow: Flow<Long?> = context.userSettingsDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_DICE_SET_ID]
        }

    suspend fun setActiveDiceSetId(diceSetId: Long) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_DICE_SET_ID] = diceSetId
        }
    }

    suspend fun clearActiveDiceSetId() {
        context.userSettingsDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACTIVE_DICE_SET_ID)
        }
    }
}
