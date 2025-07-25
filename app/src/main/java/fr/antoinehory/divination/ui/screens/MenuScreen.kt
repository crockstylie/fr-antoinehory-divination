package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
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

@Composable
fun MenuScreen(
    onNavigateToMagicBall: () -> Unit,
    onNavigateToCoinFlip: () -> Unit,
    onNavigateToRockPaperScissors: () -> Unit,
    onNavigateToDiceRoll: () -> Unit,
    onNavigateToInfo: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // S'assurer que le fond est défini
            .windowInsetsPadding(WindowInsets.safeDrawing) // APPLIQUER LES INSETS ICI
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // La Column prend toute la taille DANS les limites des insets du Box
                .padding(16.dp), // Ton padding de contenu
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.menu_title_header),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_magic_ball), onClick = onNavigateToMagicBall)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_coin_flip), onClick = onNavigateToCoinFlip)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_rps), onClick = onNavigateToRockPaperScissors)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = stringResource(id = R.string.menu_button_dice_roll), onClick = onNavigateToDiceRoll)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp), // Ajusté pour être plus près du code de ton repo
            horizontalArrangement = Arrangement.SpaceBetween, // Pour que Settings soit à gauche et Info à droite
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuIconButton(
                onClick = onNavigateToSettings, // Sera utilisé plus tard
                icon = Icons.Filled.Settings,
                contentDescription = stringResource(id = R.string.menu_settings_description) // << MODIFIÉ
            )
            MenuIconButton(
                onClick = onNavigateToInfo,
                icon = Icons.Filled.Info,
                contentDescription = stringResource(id = R.string.menu_info_description) // << MODIFIÉ
            )
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
    contentDescription: String // contentDescription est déjà une String résolue
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

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {}, {})
    }
}