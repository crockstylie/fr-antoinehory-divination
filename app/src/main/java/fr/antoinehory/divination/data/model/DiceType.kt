package fr.antoinehory.divination.data.model

/**
 * Représente les types de dés standards.
 * La valeur 'sides' correspond au nombre de faces du dé.
 */
enum class DiceType(val sides: Int) {
    D2(2),
    D3(3), // Moins courant mais existe
    D4(4), // Ajout du D4, souvent utilisé
    D6(6),
    D8(8),
    D10(10),
    D12(12),
    D20(20);

    // Propriété pour l'affichage, par exemple "D6"
    val displayName: String
        get() = "D$sides"
}

