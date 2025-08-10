package fr.antoinehory.divination.data.database.util

import androidx.room.TypeConverter
import fr.antoinehory.divination.data.model.GameType

/**
 * [TypeConverter] for Room to allow storing and retrieving [GameType] enum instances.
 * The [GameType] is stored as its [String] name (e.g., "COIN_FLIP") in the database.
 */
class GameTypeConverter {
    /**
     * Converts a [GameType] enum instance to its [String] name representation.
     *
     * @param gameType The [GameType] to convert. Can be null.
     * @return The string name of the enum constant (e.g., "DICE_ROLL"),
     *         or null if the input gameType was null.
     */
    @TypeConverter
    fun fromGameType(gameType: GameType?): String? {
        return gameType?.name
    }

    /**
     * Converts a [String] name back to a [GameType] enum instance.
     *
     * This method expects the string to be a valid name of a [GameType] enum constant.
     * If the string does not match any [GameType] constant (case-sensitive),
     * [IllegalArgumentException] will be caught by Room and it might result in `null`
     * or an error depending on the Room query context. It's generally safer if the
     * string originated from [fromGameType].
     *
     * @param name The [String] name of the enum constant to convert. Can be null.
     * @return The corresponding [GameType] enum instance, or null if the input name was null
     *         or if the name does not correspond to any [GameType] constant.
     */
    @TypeConverter
    fun toGameType(name: String?): GameType? {
        return name?.let {
            try {
                GameType.valueOf(it)
            } catch (e: IllegalArgumentException) {
                // Handle cases where the string name doesn't match any GameType enum constant
                null
            }
        }
    }
}
