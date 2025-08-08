package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold

data class BottomNavItem(
    val labelResId: Int,
    val icon: ImageVector,
    val action: () -> Unit
)

@Composable
fun BottomAppNavigationBar(
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(R.string.bottom_nav_settings, Icons.Filled.Settings, onSettingsClick),
        BottomNavItem(R.string.bottom_nav_stats, Icons.Filled.Insights, onStatsClick),
        BottomNavItem(R.string.bottom_nav_info, Icons.Filled.Info, onInfoClick)
    )

    val bottomBarHeight = 50.dp
    val iconSize = 30.dp

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .height(bottomBarHeight), // Hauteur personnalisée pour l'ensemble de la barre
        containerColor = OrakniumBackground,
        contentColor = OrakniumGold, // Couleur par défaut pour les icônes
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp) // Important: Réinitialiser le padding par défaut
    ) {
        // Utiliser une Column pour organiser la Divider et la Row des icônes
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
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically // Centre les icônes dans l'espace alloué à la Row
            ) {
                items.forEach { item ->
                    IconButton(onClick = item.action) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(id = item.labelResId),
                            modifier = Modifier.size(iconSize)
                            // La teinte est héritée de contentColor de BottomAppBar
                        )
                    }
                }
            }
        }
    }
}
