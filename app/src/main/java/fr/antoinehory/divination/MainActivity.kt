package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.divination.navigation.AppDestinations
import fr.antoinehory.divination.ui.screens.*
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

class MainActivity : ComponentActivity() {

    private val magicBallViewModel: MagicBallViewModel by viewModels()

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
                            onNavigateToInfo = { navController.navigate(AppDestinations.INFO_ROUTE) }
                        )
                    }
                    composable(AppDestinations.MAGIC_BALL_ROUTE) {
                        MagicBallScreen(
                            viewModel = magicBallViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable(AppDestinations.COIN_FLIP_ROUTE) {
                        CoinFlipScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(AppDestinations.ROCK_PAPER_SCISSORS_ROUTE) {
                        RockPaperScissorsScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(AppDestinations.DICE_ROLL_ROUTE) {
                        DiceRollScreen(onNavigateBack = { navController.popBackStack() })
                    }
                    composable(AppDestinations.INFO_ROUTE) {
                        InfoScreen(onNavigateBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}