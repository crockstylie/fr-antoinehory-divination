// In data/database/entity/LaunchLog.kt
package fr.antoinehory.divination.data.database.entity // Ou le package approprié

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.antoinehory.divination.data.model.GameType
import java.util.Date

@Entity(tableName = "launch_logs")
data class LaunchLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameType: GameType,
    val result: String, // Le résultat spécifique du lancer (ex: "Pile", "Face", "3", "Pierre", "Oui absolument")
    val timestamp: Date = Date() // Enregistre automatiquement l'heure actuelle
)
