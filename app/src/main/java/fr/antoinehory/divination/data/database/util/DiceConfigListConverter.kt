package fr.antoinehory.divination.data.database.util

import androidx.room.TypeConverter
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceType

/**
 * [TypeConverter] for Room to allow storing and retrieving lists of [DiceConfig] objects.
 * The list is stored as a single [String] in the database, with configurations
 * separated by semicolons (`;`) and dice type/count within each configuration
 * separated by colons (`:`).
 * For example: "D6:2;D20:1"
 */
class DiceConfigListConverter {

    /**
     * Converts a list of [DiceConfig] objects to its [String] representation for database storage.
     * Each [DiceConfig] is represented as "DiceType.name:count". Configurations are joined by ";".
     *
     * Example: A list containing `DiceConfig(DiceType.D6, 2)` and `DiceConfig(DiceType.D20, 1)`
     * would be converted to "D6:2;D20:1".
     *
     * @param diceConfigs The list of [DiceConfig] objects to convert. Can be null.
     * @return The string representation of the list, or null if the input list was null.
     */
    @TypeConverter
    fun fromDiceConfigList(diceConfigs: List<DiceConfig>?): String? {
        if (diceConfigs == null) {
            return null
        }
        return diceConfigs.joinToString(";") { "${it.diceType.name}:${it.count}" }
    }

    /**
     * Converts a [String] representation from the database back to a list of [DiceConfig] objects.
     * The string is expected to be in the format "DiceType.name:count;DiceType.name:count;...".
     *
     * Items that cannot be correctly parsed (e.g., incorrect format, invalid dice type name,
     * non-integer count) will be ignored and not included in the resulting list.
     * If the input data string is null, null is returned.
     * If the input data string is empty, an empty list is returned.
     * If a general exception occurs during parsing, an empty list is returned.
     *
     * @param data The [String] data to convert. Can be null or empty.
     * @return The corresponding list of [DiceConfig] objects, or null if the input data was null.
     *         Returns an empty list if data is empty or if parsing errors occur.
     */
    @TypeConverter
    fun toDiceConfigList(data: String?): List<DiceConfig>? {
        if (data == null) {
            return null
        }
        if (data.isEmpty()) {
            return emptyList()
        }
        return try {
            data.split(";").mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size == 2) {
                    try {
                        val diceTypeName = parts[0]
                        val count = parts[1].toInt()
                        DiceConfig(diceType = DiceType.valueOf(diceTypeName), count = count)
                    } catch (e: NumberFormatException) {
                        // Ignore malformed count
                        null
                    } catch (e: IllegalArgumentException) {
                        // Ignore invalid DiceType name
                        null
                    }
                } else {
                    // Ignore malformed item
                    null
                }
            }
        } catch (e: Exception) {
            // Catch any other unexpected errors during splitting/mapping and return empty list
            emptyList()
        }
    }
}

