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
import androidx.compose.ui.graphics.Color // Importer Color pour Transparent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.ui.theme.DivinationAppTheme // ou OrakniumAppTheme si tu l'as renommé
import fr.antoinehory.divination.ui.theme.OrakniumGold // Importer ta couleur

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
            .background(MaterialTheme.colorScheme.background) // Assure-toi que le fond est appliqué
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Oraknium",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground, // Devrait être OrakniumGold grâce au thème
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
                .padding(16.dp)
                .size(48.dp) // Augmenter un peu la taille de la zone cliquable
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Informations",
                modifier = Modifier.size(36.dp), // Augmenter la taille de l'icône elle-même
                tint = OrakniumGold // Appliquer la couleur OrakniumGold à l'icône
            )
        }
    }
}

// Nouveau Composable pour les boutons stylisés
@Composable
fun OrakniumMenuButton(text: String, onClick: () -> Unit) {
    OutlinedButton( // Utiliser OutlinedButton pour un fond transparent et une bordure
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(60.dp),
        shape = RoundedCornerShape(50), // Coins arrondis au maximum (pourcentage de la hauteur/2)
        border = BorderStroke(2.dp, OrakniumGold), // Bordure avec la couleur OrakniumGold
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrakniumGold, // Couleur du texte
            containerColor = Color.Transparent // Fond transparent
        )
    ) {
        Text(text = text, fontSize = 18.sp, textAlign = TextAlign.Center)
    }
}


@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    // Si tu as renommé DivinationAppTheme en OrakniumAppTheme, utilise le nouveau nom ici
    DivinationAppTheme {
        MenuScreen({}, {}, {}, {}, {})
    }
}
