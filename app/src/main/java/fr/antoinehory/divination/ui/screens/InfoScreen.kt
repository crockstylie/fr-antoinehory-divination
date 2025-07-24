package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign // Pour TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

@Composable
fun InfoScreen(onNavigateBack: () -> Unit) {
    AppScaffold(
        title = "Informations",
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp) // Padding horizontal global
                .padding(vertical = 8.dp),   // Léger padding vertical global
            horizontalAlignment = Alignment.CenterHorizontally // Centrer les InfoItemColumn
        ) {
            Text(
                "Mes Informations Personnelles",
                style = MaterialTheme.typography.headlineMedium, // Encore plus grand
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp) // Espace au-dessus et en dessous du titre principal
            )

            InfoItemColumn(label = "Développeur", value = "Antoine Crock HORY")
            InfoItemColumn(label = "Site Web", value = "antoinehory.fr", isLink = true, linkUri = "https://antoinehory.fr")
            InfoItemColumn(label = "Email", value = "contact@antoinehory.fr", isLink = true, linkUri = "mailto:contact@antoinehory.fr")
            // Ajoute d'autres informations ici en utilisant InfoItemColumn
        }
    }
}

// Nouveau Composable pour chaque item d'information avec le label au-dessus de la valeur
@Composable
fun InfoItemColumn(
    label: String,
    value: String,
    isLink: Boolean = false,
    linkUri: String? = null,
    modifier: Modifier = Modifier // Permet de passer des modificateurs externes si besoin
) {
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp) // Espace au-dessus et en dessous de chaque item complet
    ) {
        Text(
            text = "$label :", // Ajout du ":" directement ici
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp)) // Petit espace entre le label et la valeur

        if (isLink && linkUri != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = OrakniumGold, // Couleur spécifique pour le lien
                textDecoration = TextDecoration.Underline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(linkUri) }
            )
        } else {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
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
