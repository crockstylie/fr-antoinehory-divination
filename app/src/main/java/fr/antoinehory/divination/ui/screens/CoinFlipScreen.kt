package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.ui.common.AppScreen // Importe ton nouveau composable
import fr.antoinehory.divination.ui.theme.DivinationAppTheme // ou OrakniumAppTheme

@Composable
fun CoinFlipScreen(onNavigateBack: () -> Unit) {
    AppScreen(
        title = "Pile ou Face",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Important
                .padding(16.dp), // Ton padding de contenu original
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pile ou Face - À venir",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            // Tu ajouteras la logique du jeu ici
            // Plus besoin du bouton "Retour au menu" ici, la TopAppBar s'en charge.
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinFlipScreenPreview() {
    DivinationAppTheme {
        CoinFlipScreen {}
    }
}