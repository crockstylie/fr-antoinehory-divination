package fr.antoinehory.divination.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.InteractionPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Provides access to user settings stored in [DataStore].
 * This extension property creates a singleton [DataStore<Preferences>] instance
 * named "user_settings".
 */
val Context.userSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

/**
 * Repository for managing user-specific preferences using [DataStore].
 * This class handles reading and writing preferences related to interaction modes
 * and active dice sets.
 *
 * @property context The application context, used to access the [userSettingsDataStore].
 */
class UserPreferencesRepository(private val context: Context) {

    /**
     * Defines the keys used for storing and retrieving preferences in DataStore.
     */
    private object PreferencesKeys {
        /** Preference key for the active interaction mode. Stores a [String] representation of [InteractionMode]. */
        val ACTIVE_INTERACTION_MODE = stringPreferencesKey("active_interaction_mode")
        /** Preference key for the ID of the currently active dice set. Stores a [Long]. */
        val ACTIVE_DICE_SET_ID = longPreferencesKey("active_dice_set_id")
    }

    /**
     * A [Flow] that emits the user's current [InteractionPreferences].
     * It observes changes to the active interaction mode in DataStore and maps it
     * to an [InteractionPreferences] object. Defaults to [InteractionMode.TAP] if not set.
     * Emissions are filtered to only occur when the preferences actually change.
     */
    val interactionPreferencesFlow: Flow<InteractionPreferences> = context.userSettingsDataStore.data
        .map { preferences ->
            val modeString = preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] ?: InteractionMode.TAP.name
            InteractionPreferences(InteractionMode.valueOf(modeString))
        }.distinctUntilChanged()

    /**
     * Updates the active interaction mode in DataStore.
     *
     * @param mode The new [InteractionMode] to set as active.
     */
    suspend fun updateActiveInteractionMode(mode: InteractionMode) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_INTERACTION_MODE] = mode.name
        }
    }

    /**
     * A [Flow] that emits the ID of the currently active dice set.
     * Emits `null` if no dice set is currently active or selected.
     */
    val activeDiceSetIdFlow: Flow<Long?> = context.userSettingsDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACTIVE_DICE_SET_ID]
        }

    /**
     * Sets the ID of the active dice set in DataStore.
     *
     * @param diceSetId The [Long] ID of the dice set to mark as active.
     */
    suspend fun setActiveDiceSetId(diceSetId: Long) {
        context.userSettingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_DICE_SET_ID] = diceSetId
        }
    }

    /**
     * Clears the active dice set ID from DataStore.
     * This is used when no dice set should be considered active.
     */
    suspend fun clearActiveDiceSetId() {
        context.userSettingsDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.ACTIVE_DICE_SET_ID)
        }
    }
}

