package fr.antoinehory.divination.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
// MISE À JOUR DE L'IMPORTATION CI-DESSOUS :
import fr.antoinehory.divination.data.database.util.DiceConfigListConverter

// DiceConfig sera importé implicitement car il est dans le même package

/**
 * Représente un set de dés configurable par l'utilisateur.
 * Ce set peut être sauvegardé et réutilisé.
 */
@Entity(tableName = "dice_sets")
@TypeConverters(DiceConfigListConverter::class) // Utilise le convertisseur du bon package
data class DiceSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    @ColumnInfo(name = "dice_configs")
    var diceConfigs: List<DiceConfig>,
    @ColumnInfo(name = "is_favorite")
    var isFavorite: Boolean = false,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP")
    var createdAt: String? = null
) {
    val summaryDisplay: String
        get() = diceConfigs.joinToString(", ") { it.displayConfig }
}
