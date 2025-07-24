package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLifecycleOwner // Assure-toi que cet import est là si besoin pour rememberWindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.divination.navigation.AppDestinations
import fr.antoinehory.divination.ui.screens.* // Importe tes nouveaux écrans
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

class MainActivity : ComponentActivity() {

    // Le ViewModel est toujours nécessaire ici pour être passé à MagicBallScreen
    private val magicBallViewModel: MagicBallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DivinationAppTheme {
                HideSystemBars() // Ton composable pour le plein écran

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
                        // Passe le ViewModel à l'écran de la boule magique
                        MagicBallScreen(viewModel = magicBallViewModel)
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

// Garde ces Composables ici ou déplace-les dans un fichier utilitaire (par exemple, ui/utils/SystemUiUtils.kt)
@Composable
private fun HideSystemBars() {
    val windowInsetsController = rememberWindowInsetsController()
    //LaunchedEffect est préférable ici car il s'exécute lorsque la clé change ou lors de la composition initiale
    // et il est annulé lorsque le composable quitte la composition.
    androidx.compose.runtime.LaunchedEffect(Unit) {
        windowInsetsController?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}

@Composable
fun rememberWindowInsetsController(): WindowInsetsControllerCompat? {
    val window = (LocalLifecycleOwner.current as? ComponentActivity)?.window
    return window?.let { WindowCompat.getInsetsController(it, it.decorView) }
}
