package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
                            onNavigateToSettings = { navController.navigate(AppDestinations.SETTINGS_ROUTE) }
                        )
                    }
                    composable(AppDestinations.MAGIC_BALL_ROUTE) {
                        // Le ViewModel sera obtenu DANS MagicBallScreen
                        MagicBallScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(AppDestinations.COIN_FLIP_ROUTE) {
                        // Le ViewModel sera obtenu DANS CoinFlipScreen
                        CoinFlipScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(AppDestinations.ROCK_PAPER_SCISSORS_ROUTE) {
                        // Le ViewModel sera obtenu DANS RockPaperScissorsScreen
                        RockPaperScissorsScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(AppDestinations.DICE_ROLL_ROUTE) {
                        // Le ViewModel sera obtenu DANS DiceRollScreen
                        DiceRollScreen(
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(AppDestinations.INFO_ROUTE) {
                        InfoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}

