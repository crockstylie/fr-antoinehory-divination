package fr.antoinehory.divination.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.screens.*

object AppDestinations {
    const val MENU_ROUTE = "menu"
    const val MAGIC_BALL_ROUTE = "magic_ball"
    const val COIN_FLIP_ROUTE = "coin_flip"
    const val ROCK_PAPER_SCISSORS_ROUTE = "rock_paper_scissors"
    const val DICE_ROLL_ROUTE = "dice_roll"
    const val INFO_ROUTE = "info"
    const val SETTINGS_ROUTE = "settings"
    const val STATS_BASE_ROUTE = "statistics"
    const val STATS_GAME_TYPE_ARG = "gameType"
    const val STATS_ROUTE_TEMPLATE = "$STATS_BASE_ROUTE?$STATS_GAME_TYPE_ARG={$STATS_GAME_TYPE_ARG}"
    const val DICE_SET_MANAGEMENT_ROUTE = "dice_set_management"

    // Pour la création/édition des sets de dés
    const val CREATE_EDIT_DICE_SET_BASE_ROUTE = "create_edit_dice_set"
    const val DICE_SET_ID_ARG = "diceSetId"
    // Route pour la création (pas d'ID) et pour l'édition (avec ID optionnel)
    const val CREATE_EDIT_DICE_SET_ROUTE = "$CREATE_EDIT_DICE_SET_BASE_ROUTE?$DICE_SET_ID_ARG={$DICE_SET_ID_ARG}"
}

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = AppDestinations.MENU_ROUTE
    ) {
        composable(AppDestinations.MENU_ROUTE) {
            MenuScreen(
                onNavigateToMagicBall = { navController.navigate(AppDestinations.MAGIC_BALL_ROUTE) },
                onNavigateToCoinFlip = { navController.navigate(AppDestinations.COIN_FLIP_ROUTE) },
                onNavigateToRockPaperScissors = { navController.navigate(AppDestinations.ROCK_PAPER_SCISSORS_ROUTE) },
                onNavigateToDiceRoll = { navController.navigate(AppDestinations.DICE_ROLL_ROUTE) },
                onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) },
                onNavigateToSettings = { navController.navigate(AppDestinations.SETTINGS_ROUTE) },
                onNavigateToStats = { navController.navigate(AppDestinations.STATS_BASE_ROUTE) }
            )
        }
        composable(AppDestinations.MAGIC_BALL_ROUTE) {
            MagicBallScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = { gameType ->
                    navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                },
                onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) }
            )
        }
        composable(AppDestinations.COIN_FLIP_ROUTE) {
            CoinFlipScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = { gameType ->
                    navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                },
                onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) }
            )
        }
        composable(AppDestinations.ROCK_PAPER_SCISSORS_ROUTE) {
            RockPaperScissorsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = { gameType ->
                    navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                },
                onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) }
            )
        }
        composable(AppDestinations.DICE_ROLL_ROUTE) {
            DiceRollScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStats = { gameType ->
                    navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                },
                onNavigateToDiceSetManagement = { navController.navigate(AppDestinations.DICE_SET_MANAGEMENT_ROUTE) },
                onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) }
            )
        }
        composable(AppDestinations.DICE_SET_MANAGEMENT_ROUTE) {
            DiceSetManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCreateSet = {
                    // MODIFIÉ: Navigue vers l'écran de création/édition sans ID (pour création)
                    navController.navigate(AppDestinations.CREATE_EDIT_DICE_SET_BASE_ROUTE)
                },
                onLaunchSet = { diceSet ->
                    Toast.makeText(context, "Launch set: ${diceSet.name} - TODO", Toast.LENGTH_LONG).show()
                },
                // AJOUT : Action pour éditer un set existant (sera utilisée par la liste plus tard)
                onNavigateToEditSet = { diceSetId ->
                    navController.navigate("${AppDestinations.CREATE_EDIT_DICE_SET_BASE_ROUTE}?${AppDestinations.DICE_SET_ID_ARG}=${diceSetId}")
                }
            )
        }
        composable(
            route = AppDestinations.STATS_ROUTE_TEMPLATE,
            arguments = listOf(
                navArgument(AppDestinations.STATS_GAME_TYPE_ARG) {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { navBackStackEntry ->
            val gameTypeString = navBackStackEntry.arguments?.getString(AppDestinations.STATS_GAME_TYPE_ARG)
            val specificGameType = try { gameTypeString?.let { GameType.valueOf(it) } } catch (e: IllegalArgumentException) { null }
            GameStatsScreen(
                onNavigateBack = { navController.popBackStack() },
                specificGameType = specificGameType
            )
        }
        composable(AppDestinations.INFO_ROUTE) {
            InfoScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(AppDestinations.SETTINGS_ROUTE) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // AJOUT: Composable pour l'écran de création/édition de sets de dés
        composable(
            route = AppDestinations.CREATE_EDIT_DICE_SET_ROUTE,
            arguments = listOf(
                navArgument(AppDestinations.DICE_SET_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null // Explicitement null par défaut pour la création
                }
            )
        ) { navBackStackEntry ->
            val diceSetId = navBackStackEntry.arguments?.getString(AppDestinations.DICE_SET_ID_ARG)
            CreateEditDiceSetScreen(
                onNavigateBack = { navController.popBackStack() },
                diceSetId = diceSetId
            )
        }
    }
}
