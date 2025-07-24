package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

@Composable
fun MenuScreen(
    onNavigateToMagicBall: () -> Unit,
    onNavigateToCoinFlip: () -> Unit,
    onNavigateToRockPaperScissors: () -> Unit,
    onNavigateToDiceRoll: () -> Unit,
    onNavigateToInfo: () -> Unit
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
                text = "Oraknium",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            OrakniumMenuButton(text = "Boule Magique N°8", onClick = onNavigateToMagicBall)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = "Pile ou Face", onClick = onNavigateToCoinFlip)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = "Shifoumi", onClick = onNavigateToRockPaperScissors)
            Spacer(modifier = Modifier.height(16.dp))
            OrakniumMenuButton(text = "Lancer de Dés", onClick = onNavigateToDiceRoll)
        }

        IconButton(
            onClick = onNavigateToInfo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp) // Ce padding est relatif au Box parent (qui a déjà les insets)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Informations",
                modifier = Modifier.size(36.dp),
                tint = OrakniumGold
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

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {})
    }
}