package fr.antoinehory.divination.data.model

import fr.antoinehory.divination.R

/**
 * Represents the different types of games available in the application.
 * Each game type corresponds to a distinct mini-game or divination method.
 */
enum class GameType {
    /** Represents the Coin Flip game. */
    COIN_FLIP,
    /** Represents the Dice Roll game. */
    DICE_ROLL,
    /** Represents the Rock Paper Scissors game. */
    ROCK_PAPER_SCISSORS,
    /** Represents the Magic 8-Ball game. */
    MAGIC_EIGHT_BALL
}

/**
 * Gets the string resource ID for the display name of this [GameType].
 * This can be used to show a localized title for the game screen.
 */
val GameType.displayNameResourceId: Int
    get() = when (this) {
        GameType.COIN_FLIP -> R.string.coin_flip_screen_title
        GameType.MAGIC_EIGHT_BALL -> R.string.magic_ball_screen_title
        GameType.DICE_ROLL -> R.string.dice_roll_screen_title
        GameType.ROCK_PAPER_SCISSORS -> R.string.rps_screen_title
    }