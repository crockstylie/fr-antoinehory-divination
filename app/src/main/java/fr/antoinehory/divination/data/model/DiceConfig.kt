package fr.antoinehory.divination.data.model

import java.util.UUID

// DiceType sera importé implicitement car il est dans le même package

/**
 * Représente la configuration d'un type de dé au sein d'un set.
 * Par exemple, "2 dés de type D6".
 */
data class DiceConfig(
    val id: String = UUID.randomUUID().toString(),
    val diceType: DiceType,
    val count: Int
) {
    // Propriété pour l'affichage, par exemple "2 x D6"
    val displayConfig: String
        get() = "$count x ${diceType.displayName}"
}