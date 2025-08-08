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
import androidx.compose.ui.graphics.vector.ImageVector // Conservé au cas où, mais BottomNavItem n'est plus utilisé
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold

// BottomNavItem n'est plus utilisé ici, mais je le laisse au cas où vous en auriez l'usage ailleurs.
// data class BottomNavItem(
//    val labelResId: Int,
//    val icon: ImageVector,
//    val action: () -> Unit
// )

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
        contentColor = OrakniumGold, // Couleur par défaut pour les icônes
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp) // Important: Réinitialiser le padding par défaut
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Divider(
                color = OrakniumGold,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // La Row prend l'espace vertical restant après la Divider
                horizontalArrangement = Arrangement.SpaceAround, // Conserve 3 "slots"
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 1: Bouton Settings (ou placeholder invisible)
                val settingsAlpha = if (showSettingsButton) 1f else 0f
                val settingsEnabled = showSettingsButton

                IconButton(
                    onClick = onSettingsClick, // L'action est toujours là, mais enabled contrôle l'interaction
                    enabled = settingsEnabled,
                    modifier = Modifier.alpha(settingsAlpha) // Rend invisible si showSettingsButton est faux
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.bottom_nav_settings),
                        modifier = Modifier.size(iconSize)
                        // La teinte est héritée
                    )
                }

                // Slot 2: Bouton Stats (toujours visible)
                IconButton(onClick = onStatsClick) {
                    Icon(
                        imageVector = Icons.Filled.Insights,
                        contentDescription = stringResource(id = R.string.bottom_nav_stats),
                        modifier = Modifier.size(iconSize)
                    )
                }

                // Slot 3: Bouton Info (toujours visible)
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