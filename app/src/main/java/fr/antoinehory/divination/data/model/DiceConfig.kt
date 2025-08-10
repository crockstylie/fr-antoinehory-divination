package fr.antoinehory.divination.data.model

import java.util.UUID

/**
 * Represents the configuration for a specific type of die within a [DiceSet].
 * For example, "2 dice of type D6" or "1 die of type D20".
 *
 * @property id A unique identifier for this dice configuration. Automatically generated.
 * @property diceType The [DiceType] (e.g., D4, D6, D20) for this configuration.
 * @property count The number of dice of this [diceType] to include. Should be at least 1.
 */
data class DiceConfig(
    val id: String = UUID.randomUUID().toString(),
    val diceType: DiceType,
    val count: Int
) {
    /**
     * A formatted string for displaying this dice configuration.
     * For example, "2 x D6" or "1 x D20".
     */
    val displayConfig: String
        get() = "$count x ${diceType.displayName}"
}
