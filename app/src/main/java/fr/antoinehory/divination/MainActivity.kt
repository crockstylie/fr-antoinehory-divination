package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.navigation.AppDestinations
import fr.antoinehory.divination.ui.screens.*
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DivinationAppTheme {
                val navController = rememberNavController()

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
                            }
                        )
                    }
                    composable(AppDestinations.COIN_FLIP_ROUTE) {
                        CoinFlipScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStats = { gameType ->
                                navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                            }
                        )
                    }
                    composable(AppDestinations.ROCK_PAPER_SCISSORS_ROUTE) {
                        RockPaperScissorsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStats = { gameType ->
                                navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                            }
                        )
                    }
                    composable(AppDestinations.DICE_ROLL_ROUTE) {
                        DiceRollScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToStats = { gameType -> // AJOUTÉ
                                navController.navigate("${AppDestinations.STATS_BASE_ROUTE}?${AppDestinations.STATS_GAME_TYPE_ARG}=${gameType.name}")
                            }
                        )
                    }
                    composable(AppDestinations.INFO_ROUTE) {
                        InfoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(AppDestinations.SETTINGS_ROUTE) {
                        SettingsScreen(onNavigateBack = { navController.popBackStack() })
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
                        val specificGameType = try {
                            gameTypeString?.let { GameType.valueOf(it) }
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                        GameStatsScreen(
                            onNavigateBack = { navController.popBackStack() },
                            specificGameType = specificGameType
                        )
                    }
                }
            }
        }
    }
}
