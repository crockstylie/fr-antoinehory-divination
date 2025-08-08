package fr.antoinehory.divination.data.database.util // Correction du package

import androidx.room.TypeConverter
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceType

class DiceConfigListConverter {

    @TypeConverter
    fun fromDiceConfigList(diceConfigs: List<DiceConfig>?): String? {
        if (diceConfigs == null) {
            return null
        }
        // Convertit List<DiceConfig> en une chaîne. Exemple: "D6:2;D20:1"
        // La sérialisation n'a pas besoin de changer car elle n'utilise pas le constructeur avec id
        return diceConfigs.joinToString(";") { "${it.diceType.name}:${it.count}" }
    }

    @TypeConverter
    fun toDiceConfigList(data: String?): List<DiceConfig>? {
        if (data == null) {
            return null
        }
        if (data.isEmpty()) { // Gérer le cas où la chaîne est vide mais pas nulle
            return emptyList()
        }
        // Reconvertit la chaîne en List<DiceConfig>
        return try {
            data.split(";").mapNotNull { item ->
                val parts = item.split(":")
                if (parts.size == 2) {
                    try {
                        val diceTypeName = parts[0]
                        val count = parts[1].toInt()
                        // MODIFIÉ ICI: Utilisation d'arguments nommés
                        DiceConfig(diceType = DiceType.valueOf(diceTypeName), count = count)
                    } catch (e: NumberFormatException) {
                        // Gérer l'erreur si le compte n'est pas un nombre valide
                        null // ou logger l'erreur et ignorer l'item
                    } catch (e: IllegalArgumentException) {
                        // Gérer l'erreur si diceTypeName n'est pas un DiceType valide
                        null // ou logger l'erreur
                    }
                } else {
                    null // Item malformé
                }
            }
        } catch (e: Exception) {
            // Gérer toute autre exception inattendue pendant le parsing
            emptyList() // ou null, ou logger l'erreur
        }
    }
}
