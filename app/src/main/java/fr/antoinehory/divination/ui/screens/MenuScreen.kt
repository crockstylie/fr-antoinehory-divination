package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

/**
 * Composable screen that serves as the main menu of the application.
 * It displays a list of buttons for navigating to different game screens
 * and a bottom navigation bar for accessing settings, stats, and info sections.
 *
 * @param onNavigateToMagicBall Callback to navigate to the Magic 8-Ball screen.
 * @param onNavigateToCoinFlip Callback to navigate to the Coin Flip screen.
 * @param onNavigateToRockPaperScissors Callback to navigate to the Rock Paper Scissors screen.
 * @param onNavigateToDiceRoll Callback to navigate to the Dice Roll screen.
 * @param onNavigateToInfo Callback to navigate to the Information screen.
 * @param onNavigateToSettings Callback to navigate to the Settings screen (typically app settings or game-specific settings like dice set management).
 * @param onNavigateToStats Callback to navigate to the Statistics screen.
 */
@OptIn(ExperimentalMaterial3Api::class) // For Scaffold
@Composable
fun MenuScreen(
    onNavigateToMagicBall: () -> Unit,
    onNavigateToCoinFlip: () -> Unit,
    onNavigateToRockPaperScissors: () -> Unit,
    onNavigateToDiceRoll: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = onNavigateToSettings,
                onStatsClick = onNavigateToStats,
                onInfoClick = onNavigateToInfo
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from the Scaffold.
                .verticalScroll(rememberScrollState()) // Allow content to be scrollable.
                .padding(start = 16.dp, end = 16.dp, top = 16.dp), // Inner padding for the Column content.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main title header of the menu screen.
            Text(
                text = stringResource(id = R.string.menu_title_header),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Navigation buttons for each game/feature.
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_magic_ball), onClick = onNavigateToMagicBall)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_coin_flip), onClick = onNavigateToCoinFlip)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_rps), onClick = onNavigateToRockPaperScissors)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_dice_roll), onClick = onNavigateToDiceRoll)
            Spacer(modifier = Modifier.height(16.dp)) // Spacer at the end for visual padding.
        }
    }
}

/**
 * A custom styled [OutlinedButton] used for menu navigation.
 * It features a specific shape, border, and colors consistent with the Oraknium theme.
 *
 * @param text The text to display on the button.
 * @param onClick Lambda function to be invoked when the button is clicked.
 */
@Composable
fun OrakniumMenuButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f) // Button takes 80% of the available width.
            .height(60.dp),     // Fixed height for the button.
        shape = RoundedCornerShape(50), // Fully rounded corners.
        border = BorderStroke(2.dp, OrakniumGold), // Custom border using theme color.
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrakniumGold, // Text color.
            containerColor = Color.Transparent // Transparent background for outlined style.
        )
    ) {
        Text(text = text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

/**
 * Preview composable for the [MenuScreen] in portrait orientation.
 * Provides a visual representation of the menu screen with no-op navigation lambdas.
 */
@Preview(showBackground = true, name = "Menu Screen Portrait")
@Composable
fun MenuScreenPreviewPortrait() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {}, {}, {})
    }
}

/**
 * Preview composable for the [MenuScreen] in landscape orientation.
 * Demonstrates how the screen layout adapts to different width/height ratios.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Menu Screen Landscape")
@Composable
fun MenuScreenPreviewLandscape() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {}, {}, {})
    }
}

