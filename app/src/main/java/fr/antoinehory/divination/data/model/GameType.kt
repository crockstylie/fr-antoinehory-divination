// In data/model/GameType.kt
package fr.antoinehory.divination.data.model // Ou le package approprié

import fr.antoinehory.divination.R

enum class GameType {
    COIN_FLIP,       // Pile ou Face
    DICE_ROLL,       // Lancer de Dé
    ROCK_PAPER_SCISSORS, // Pierre Feuille Ciseaux
    MAGIC_EIGHT_BALL // Magic 8 Ball
    // Ajoutez d'autres types de jeux ici si nécessaire
}

// Ajoutez ceci à la fin du fichier :
val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
    }