// In data/database/util/GameTypeConverter.kt
package fr.antoinehory.divination.data.database.util

import androidx.room.TypeConverter
import fr.antoinehory.divination.data.model.GameType

class GameTypeConverter {
    @TypeConverter
    fun fromGameType(gameType: GameType?): String? {
        return gameType?.name
    }

    @TypeConverter
    fun toGameType(name: String?): GameType? {
        return name?.let { GameType.valueOf(it) }
    }
}
