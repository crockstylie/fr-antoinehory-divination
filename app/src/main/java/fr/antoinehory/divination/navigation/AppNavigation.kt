package fr.antoinehory.divination.navigation

object AppDestinations {
    const val MENU_ROUTE = "menu"
    const val MAGIC_BALL_ROUTE = "magic_ball"
    const val COIN_FLIP_ROUTE = "coin_flip"
    const val ROCK_PAPER_SCISSORS_ROUTE = "rock_paper_scissors"
    const val DICE_ROLL_ROUTE = "dice_roll"
    const val INFO_ROUTE = "info"
    const val SETTINGS_ROUTE = "settings"

    // Nouvelles routes pour les statistiques
    const val STATS_BASE_ROUTE = "statistics" // Route de base
    const val STATS_GAME_TYPE_ARG = "gameType" // Nom de l'argument
    // Route complète avec argument optionnel. Ex: "statistics" ou "statistics?gameType=COIN_FLIP"
    const val STATS_ROUTE_TEMPLATE = "$STATS_BASE_ROUTE?$STATS_GAME_TYPE_ARG={$STATS_GAME_TYPE_ARG}"
}