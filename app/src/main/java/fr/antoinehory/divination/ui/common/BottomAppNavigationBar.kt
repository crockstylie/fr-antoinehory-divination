package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold

/**
 * A composable function that displays a bottom application navigation bar.
 * It includes icons for navigating to Settings, Stats, and Info sections of the app.
 * A divider is shown at the top of the bar.
 *
 * @param onSettingsClick Lambda to be invoked when the Settings icon is clicked.
 * @param onStatsClick Lambda to be invoked when the Stats (Insights) icon is clicked.
 * @param onInfoClick Lambda to be invoked when the Info icon is clicked.
 * @param modifier [Modifier] to be applied to the bottom navigation bar. Defaults to [Modifier].
 * @param showSettingsButton Boolean to control the visibility and interactivity of the Settings button.
 *                           If false, the button is hidden (alpha set to 0f) and disabled. Defaults to true.
 */
@Composable
fun BottomAppNavigationBar(
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSettingsButton: Boolean = true
) {
    val bottomBarHeight = 50.dp
    val iconSize = 30.dp

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(bottomBarHeight),
        containerColor = OrakniumBackground,
        contentColor = OrakniumGold,
        tonalElevation = 0.dp, // No shadow/elevation
        contentPadding = PaddingValues(0.dp) // No internal padding for the BottomAppBar itself
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Visual separator at the top of the navigation bar
            Divider(
                color = OrakniumGold,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes up the remaining vertical space after the divider
                horizontalArrangement = Arrangement.SpaceAround, // Distributes icons evenly
                verticalAlignment = Alignment.CenterVertically // Centers icons vertically
            ) {
                // Settings button is conditionally displayed based on showSettingsButton
                val settingsAlpha = if (showSettingsButton) 1f else 0f
                val settingsEnabled = showSettingsButton

                IconButton(
                    onClick = onSettingsClick,
                    enabled = settingsEnabled,
                    modifier = Modifier.alpha(settingsAlpha) // Controls visibility via transparency
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.bottom_nav_settings),
                        modifier = Modifier.size(iconSize)
                    )
                }

                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Filled.Insights,
                        contentDescription = stringResource(id = R.string.bottom_nav_stats),
                        modifier = Modifier.size(iconSize)
                    )
                }

                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = stringResource(id = R.string.bottom_nav_info),
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
