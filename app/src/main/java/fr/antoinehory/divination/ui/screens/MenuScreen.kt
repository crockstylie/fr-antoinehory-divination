package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.* // Scaffold, BottomAppBar sont ici
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

@OptIn(ExperimentalMaterial3Api::class) // Nécessaire pour Scaffold et TopAppBar/BottomAppBar
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
        containerColor = MaterialTheme.colorScheme.background, // Couleur de fond principale
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background, // Fond opaque pour la barre du bas
                contentColor = OrakniumGold // Couleur par défaut pour le contenu, mais vos icônes ont déjà un tint
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), // Padding pour les icônes à l'intérieur de la BottomAppBar
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MenuIconButton(
                        onClick = onNavigateToSettings,
                        icon = Icons.Filled.Settings,
                        contentDescription = stringResource(id = R.string.menu_settings_description)
                    )
                    MenuIconButton(
                        onClick = onNavigateToStats,
                        icon = Icons.Filled.PieChart,
                        contentDescription = stringResource(id = R.string.menu_stats_description)
                    )
                    MenuIconButton(
                        onClick = onNavigateToInfo,
                        icon = Icons.Filled.Info,
                        contentDescription = stringResource(id = R.string.menu_info_description)
                    )
                }
            }
        }
    ) { paddingValues -> // paddingValues fournies par le Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Appliquer le padding du Scaffold (gère les insets et la BottomAppBar)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp), // Padding interne pour le contenu, bottom est géré par paddingValues
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.menu_title_header),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_magic_ball), onClick = onNavigateToMagicBall)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_coin_flip), onClick = onNavigateToCoinFlip)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_rps), onClick = onNavigateToRockPaperScissors)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_dice_roll), onClick = onNavigateToDiceRoll)
            // Le padding en bas sera géré par les paddingValues du Scaffold pour laisser de la place à la BottomAppBar
            // Un Spacer supplémentaire peut être ajouté ici si plus d'espace est souhaité avant la BottomAppBar,
            // même avec le padding des paddingValues. Par exemple :
            Spacer(modifier = Modifier.height(16.dp)) // Pour un peu d'espace avant que le contenu ne "touche" la zone de la bottom bar
        }
    }
}

@Composable
fun OrakniumMenuButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(60.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(2.dp, OrakniumGold),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrakniumGold,
            containerColor = Color.Transparent
        )
    ) {
        Text(text = text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MenuIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(36.dp),
            tint = OrakniumGold
        )
    }
}

@Preview(showBackground = true, name = "Menu Screen Portrait")
@Composable
fun MenuScreenPreviewPortrait() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Menu Screen Landscape")
@Composable
fun MenuScreenPreviewLandscape() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {}, {}, {})
    }
}
