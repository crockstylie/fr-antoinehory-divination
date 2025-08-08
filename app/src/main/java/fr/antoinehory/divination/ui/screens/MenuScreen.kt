package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.background // Moins susceptible d'être nécessaire pour la bottom bar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
// import androidx.compose.material.icons.Icons // Importations spécifiques d'icônes plus nécessaires ici
// import androidx.compose.material.icons.filled.Info // Géré par BottomAppNavigationBar
// import androidx.compose.material.icons.filled.PieChart // Géré par BottomAppNavigationBar (probablement avec une autre icône)
// import androidx.compose.material.icons.filled.Settings // Géré par BottomAppNavigationBar
import androidx.compose.material3.* // Scaffold reste, BottomAppBar est maintenant dans BottomAppNavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.vector.ImageVector // Plus nécessaire pour MenuIconButton
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar // AJOUT: Import
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold // Conservé pour OrakniumMenuButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    onNavigateToMagicBall: () -> Unit,
    onNavigateToCoinFlip: () -> Unit,
    onNavigateToRockPaperScissors: () -> Unit,
    onNavigateToDiceRoll: () -> Unit,
    // Les paramètres suivants sont pour la BottomAppNavigationBar
    onNavigateToInfo: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // MODIFICATION: Utilisation de la barre de navigation commune
            BottomAppNavigationBar(
                onSettingsClick = onNavigateToSettings, // Pointe vers les paramètres généraux depuis MenuScreen
                onStatsClick = onNavigateToStats,       // Pointe vers les stats globales depuis MenuScreen
                onInfoClick = onNavigateToInfo
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OrakniumMenuButton(text: String, onClick: () -> Unit) { // Conservé tel quel
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

// Le composable MenuIconButton n'est plus utilisé et peut être supprimé.
// @Composable
// private fun MenuIconButton(
//    onClick: () -> Unit,
//    icon: ImageVector,
//    contentDescription: String
// ) { ... }

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

