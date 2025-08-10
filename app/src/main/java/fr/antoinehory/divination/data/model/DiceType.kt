package fr.antoinehory.divination.data.model

/**
 * Represents the different types of dice available in the application,
 * categorized by their number of sides.
 *
 * @property sides The number of sides for this type of die.
 */
enum class DiceType(val sides: Int) {
    /** A 2-sided die (like a coin). */
    D2(2),
    /** A 3-sided die. */
    D3(3),
    /** A 4-sided die (tetrahedron). */
    D4(4),
    /** A 6-sided die (standard cube). */
    D6(6),
    /** An 8-sided die (octahedron). */
    D8(8),
    /** A 10-sided die (pentagonal trapezohedron). */
    D10(10),
    /** A 12-sided die (dodecahedron). */
    D12(12),
    /** A 20-sided die (icosahedron). */
    D20(20);

    /**
     * A user-friendly display name for the dice type.
     * For example, D6 will be displayed as "D6".
     */
    val displayName: String
        get() = "D$sides"
}