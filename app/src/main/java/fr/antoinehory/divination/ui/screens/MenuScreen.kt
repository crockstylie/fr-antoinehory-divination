package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

@Composable
fun MenuScreen(
    onNavigateToMagicBall: () -> Unit,
    onNavigateToCoinFlip: () -> Unit,
    onNavigateToRockPaperScissors: () -> Unit,
    onNavigateToDiceRoll: () -> Unit,
    onNavigateToInfo: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Divination", // Ou le nom de ton application
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 50.dp)
            )

            MenuButton(text = "Boule Magique N°8", onClick = onNavigateToMagicBall)
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = "Pile ou Face", onClick = onNavigateToCoinFlip)
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = "Shifoumi", onClick = onNavigateToRockPaperScissors)
            Spacer(modifier = Modifier.height(16.dp))
            MenuButton(text = "Lancer de Dés", onClick = onNavigateToDiceRoll)
        }

        IconButton(
            onClick = onNavigateToInfo,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Informations",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f) // Les boutons prennent 80% de la largeur
            .height(60.dp),
        shape = MaterialTheme.shapes.medium
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