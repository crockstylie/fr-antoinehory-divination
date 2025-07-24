package fr.antoinehory.divination.ui.screens

// At the top of InfoScreen.kt
import fr.antoinehory.divination.ui.theme.OrakniumGold
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Informations") }, // Le texte sera OrakniumGold
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = OrakniumGold // Assure-toi que l'icône de retour est aussi dorée
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // Ou OrakniumBackground si tu veux qu'elle se fonde
                    titleContentColor = MaterialTheme.colorScheme.onSurface, // Devrait être OrakniumGold
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface // Devrait être OrakniumGold
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Important pour que le contenu ne soit pas sous la TopAppBar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // ou Center si tu préfères
        ) {
            Text("Mes Informations Personnelles", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Développeur : Ton Nom / Pseudo")
            Text("Site Web : ton-site.com")
            Text("Email : ton-email@example.com")
            Text("Réseaux Sociaux : @tonprofil")
            // Ajoute d'autres informations ici
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    DivinationAppTheme {
        InfoScreen {}
    }
}