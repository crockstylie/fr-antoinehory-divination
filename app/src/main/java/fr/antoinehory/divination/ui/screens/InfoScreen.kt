package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // Potentiellement pour d'autres icônes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Si vous voulez utiliser Color.Unspecified
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R // Important pour accéder à vos drawables
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
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Mes Informations Personnelles",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            InfoItemColumn(label = "Développeur", value = "Antoine Crock HORY")
            InfoItemColumn(label = "Site Web", value = "antoinehory.fr", isLink = true, linkUri = "https://antoinehory.fr")
            InfoItemColumn(label = "Email", value = "contact@antoinehory.fr", isLink = true, linkUri = "mailto:contact@antoinehory.fr")
            InfoItemColumn(label = "Donate", value = "paypal.me/kuroku", isLink = true, linkUri = "https://paypal.me/kuroku")

            Spacer(modifier = Modifier.height(24.dp)) // Espace avant les icônes sociales

            Text(
                "Retrouvez-moi sur :", // Titre pour la section des réseaux sociaux
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialMediaIcon(
                    iconResId = R.drawable.ic_linkedin_logo,
                    contentDescription = "LinkedIn Profile",
                    url = "https://www.linkedin.com/in/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_behance_logo,
                    contentDescription = "Behance Profile",
                    url = "https://www.behance.net/antoine-hory"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_instagram_logo,
                    contentDescription = "Instagram Profile",
                    url = "https://www.instagram.com/antoine.hory.web/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_facebook_logo, 
                    contentDescription = "Facebook Profile",
                    url = "https://www.facebook.com/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_spotify_logo,  
                    contentDescription = "Spotify Profile",
                    url = "https://open.spotify.com/user/crockstylie"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_steam_logo, 
                    contentDescription = "Steam Profile",
                    url = "https://steamcommunity.com/id/crockstylie/"
                )
            }
        }
    }
}

@Composable
fun InfoItemColumn(
    label: String,
    value: String,
    isLink: Boolean = false,
    linkUri: String? = null,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "$label :",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))

        if (isLink && linkUri != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = OrakniumGold,
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

@Composable
fun SocialMediaIcon(
    iconResId: Int,
    contentDescription: String,
    url: String,
    modifier: Modifier = Modifier // Permet de passer des modificateurs pour la taille etc.
) {
    val uriHandler = LocalUriHandler.current
    IconButton(
        onClick = { uriHandler.openUri(url) },
        modifier = modifier // Appliquer le modificateur ici
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified, // Ou MaterialTheme.colorScheme.primary si vous voulez les teinter
            modifier = Modifier.size(36.dp) // Ajustez la taille selon vos besoins
        )
    }
}


@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {
    DivinationAppTheme {
        // Pour la preview, vous devrez peut-être créer des drawables factices
        // ou commenter la section SocialMediaIcon si les R.drawable ne sont pas résolus en mode preview
        // sans une build complète.
        InfoScreen {}
    }
}

