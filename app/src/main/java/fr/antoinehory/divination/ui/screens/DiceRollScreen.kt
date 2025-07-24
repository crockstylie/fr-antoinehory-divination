package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

@Composable
fun DiceRollScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Lancer de Dés", style = MaterialTheme.typography.headlineMedium)
        // Tu ajouteras la logique du jeu ici
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onNavigateBack) {
            Text("Retour au menu")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceRollScreenPreview() {
    DivinationAppTheme {
        DiceRollScreen {}
    }
}