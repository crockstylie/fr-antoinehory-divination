package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // AJOUT
import androidx.compose.foundation.verticalScroll // AJOUT
import androidx.compose.material.icons.Icons // Potentiellement pour d'autres icônes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Si vous voulez utiliser Color.Unspecified
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
        title = stringResource(R.string.informations),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding du Scaffold
                .verticalScroll(rememberScrollState()) // Rend la colonne défilable
                .padding(horizontal = 16.dp) // Padding horizontal pour le contenu
                .padding(bottom = 16.dp), // Padding en bas pour le dernier élément
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.mes_informations_personnelles),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            InfoItemColumn(label = stringResource(R.string.d_veloppeur), value = "Antoine Crock HORY")
            InfoItemColumn(label = stringResource(R.string.site_web), value = "antoinehory.fr", isLink = true, linkUri = "https://antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.email), value = "contact@antoinehory.fr", isLink = true, linkUri = "mailto:contact@antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.donate), value = "paypal.me/kuroku", isLink = true, linkUri = "https://paypal.me/kuroku")

            Spacer(modifier = Modifier.height(24.dp)) // Espace avant les icônes sociales

            Text(
                stringResource(R.string.retrouvez_moi_sur), // Titre pour la section des réseaux sociaux
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly, // Conserver SpaceEvenly pour le paysage si assez de place
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialMediaIcon(
                    iconResId = R.drawable.ic_linkedin_logo,
                    contentDescription = stringResource(R.string.linkedin_profile),
                    url = "https://www.linkedin.com/in/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_behance_logo,
                    contentDescription = stringResource(R.string.behance_profile),
                    url = "https://www.behance.net/antoine-hory"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_instagram_logo,
                    contentDescription = stringResource(R.string.instagram_profile),
                    url = "https://www.instagram.com/antoine.hory.web/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_facebook_logo,
                    contentDescription = stringResource(R.string.facebook_profile),
                    url = "https://www.facebook.com/antoinehory/"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_spotify_logo,
                    contentDescription = stringResource(R.string.spotify_profile),
                    url = "https://open.spotify.com/user/crockstylie"
                )
                SocialMediaIcon(
                    iconResId = R.drawable.ic_steam_logo,
                    contentDescription = stringResource(R.string.steam_profile),
                    url = "https://steamcommunity.com/id/crockstylie/"
                )
            }
            // Le padding en bas de la Column défilable (16.dp) devrait suffire.
            // Si la Row des icônes est très haute ou si plus d'espace est nécessaire en mode paysage,
            // un Spacer(Modifier.height(16.dp)) pourrait être ajouté ici aussi.
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
            .padding(vertical = 12.dp) // Changé de 8.dp à 12.dp pour un peu plus d'espace vertical
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
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    IconButton(
        onClick = { uriHandler.openUri(url) },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(40.dp)
        )
    }
}


@Preview(showBackground = true, name = "Info Screen Portrait")
@Composable
fun InfoScreenPreview() {
    DivinationAppTheme {
        InfoScreen {}
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Info Screen Landscape")
@Composable
fun InfoScreenPreviewLandscape() {
    DivinationAppTheme {
        InfoScreen {}
    }
}