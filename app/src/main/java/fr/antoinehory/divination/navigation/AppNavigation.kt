package fr.antoinehory.divination.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.screens.*

/**
 * Defines all navigation destinations and argument keys used in the application.
 * This object serves as a single source of truth for navigation routes,
 * ensuring consistency and preventing typos when navigating.
 */
object AppDestinations {
    /** Route for the main menu screen. */
    const val MENU_ROUTE = "menu"
    /** Route for the Magic 8-Ball game screen. */
    const val MAGIC_BALL_ROUTE = "magic_ball"
    /** Route for the Coin Flip game screen. */
    const val COIN_FLIP_ROUTE = "coin_flip"
    /** Route for the Rock Paper Scissors game screen. */
    const val ROCK_PAPER_SCISSORS_ROUTE = "rock_paper_scissors"
    /** Route for the Dice Roll game screen. */
    const val DICE_ROLL_ROUTE = "dice_roll"
    /** Route for the information screen. */
    const val INFO_ROUTE = "info"
    /** Route for the settings screen. */
    const val SETTINGS_ROUTE = "settings"

    /** Base route for the game statistics screen. Can be used to show general stats. */
    const val STATS_BASE_ROUTE = "statistics"
    /** Navigation argument key for the game type when navigating to the statistics screen. */
    const val STATS_GAME_TYPE_ARG = "gameType"
    /**
     * Route template for the game statistics screen, allowing an optional game type argument.
     * Example: "statistics?gameType=COIN_FLIP"
     */
    const val STATS_ROUTE_TEMPLATE = "$STATS_BASE_ROUTE?$STATS_GAME_TYPE_ARG={$STATS_GAME_TYPE_ARG}"

    /** Route for the dice set management screen. */
    const val DICE_SET_MANAGEMENT_ROUTE = "dice_set_management"

    /** Base route for the screen to create or edit a dice set. */
    const val CREATE_EDIT_DICE_SET_BASE_ROUTE = "create_edit_dice_set"
    /** Navigation argument key for the dice set ID when navigating to the create/edit screen. */
    const val DICE_SET_ID_ARG = "diceSetId"
    /**
     * Route template for creating or editing a dice set, allowing an optional dice set ID argument.
     * If [DICE_SET_ID_ARG] is provided, it's for editing an existing set. Otherwise, it's for creating a new one.
     * Example: "create_edit_dice_set?diceSetId=123"
     */
    const val CREATE_EDIT_DICE_SET_ROUTE = "$CREATE_EDIT_DICE_SET_BASE_ROUTE?$DICE_SET_ID_ARG={$DICE_SET_ID_ARG}"
}

/**
 * Composable function that sets up the main navigation graph for the application.
 * It uses a [NavHost] to define all possible navigation paths and the composable
 * screens associated with each route.
 *
 * @param navController The [NavHostController] that manages app navigation.
 */
@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current // Typically used for ViewModel factories or resource access

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
                    navController.navigate(AppDestinations.CREATE_EDIT_DICE_SET_BASE_ROUTE) // Navigate without ID for creation
                },
                onLaunchSet = { /* diceSet -> Bug: Parameter not used, but likely intended for navigation or ViewModel interaction */
                    // Consider navigating to DICE_ROLL_ROUTE with the selected diceSet or its ID
                    navController.navigate(AppDestinations.DICE_ROLL_ROUTE)
                },
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
                    nullable = true // Game type is optional for general statistics
                    defaultValue = null
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

        composable(
            route = AppDestinations.CREATE_EDIT_DICE_SET_ROUTE,
            arguments = listOf(
                navArgument(AppDestinations.DICE_SET_ID_ARG) {
                    type = NavType.StringType // ID is passed as string, can be converted to Long later if needed
                    nullable = true // ID is optional; null for creation, present for editing
                    defaultValue = null
                }
            )
        ) { navBackStackEntry ->
            val diceSetId = navBackStackEntry.arguments?.getString(AppDestinations.DICE_SET_ID_ARG)
            CreateEditDiceSetScreen(
                onNavigateBack = { navController.popBackStack() },
                diceSetId = diceSetId // Pass the string ID, screen will handle conversion/logic
            )
        }
    }
}
