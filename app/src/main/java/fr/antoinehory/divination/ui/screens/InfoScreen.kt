package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

/**
 * Composable screen that displays information about the application and its developer.
 * It includes personal information, links to websites, email, donation, and social media profiles.
 *
 * @param onNavigateBack Callback function to handle navigation back to the previous screen.
 */
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
                .padding(paddingValues) // Apply padding from the Scaffold.
                .verticalScroll(rememberScrollState()) // Allow content to be scrollable.
                .padding(horizontal = 16.dp) // Horizontal padding for the content.
                .padding(bottom = 16.dp), // Bottom padding for the content.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Section title for personal information.
            Text(
                stringResource(R.string.mes_informations_personnelles),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            )

            // Display individual pieces of information using InfoItemColumn.
            InfoItemColumn(label = stringResource(R.string.d_veloppeur), value = "Antoine Crock HORY")
            InfoItemColumn(label = stringResource(R.string.site_web), value = "antoinehory.fr", isLink = true, linkUri = "https://antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.email), value = "contact@antoinehory.fr", isLink = true, linkUri = "mailto:contact@antoinehory.fr")
            InfoItemColumn(label = stringResource(R.string.donate), value = "paypal.me/kuroku", isLink = true, linkUri = "https://paypal.me/kuroku")

            Spacer(modifier = Modifier.height(24.dp)) // Spacer between sections.

            // Section title for social media links.
            Text(
                stringResource(R.string.retrouvez_moi_sur),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Row containing social media icons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly, // Distribute icons evenly.
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
        }
    }
}

/**
 * A composable that displays a labeled piece of information, potentially as a clickable link.
 * It's structured as a column with the label above the value.
 *
 * @param label The label text for the information (e.g., "Developer", "Website").
 * @param value The actual information text to display.
 * @param isLink If true, the [value] text will be styled as a link and will open [linkUri] when clicked. Defaults to false.
 * @param linkUri The URI string to open if [isLink] is true and the item is clicked. Defaults to null.
 * @param modifier Optional [Modifier] to be applied to the root Column of this composable.
 */
@Composable
fun InfoItemColumn(
    label: String,
    value: String,
    isLink: Boolean = false,
    linkUri: String? = null,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current // Used to open URIs.

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp) // Vertical padding for each info item.
    ) {
        // Display the label.
        Text(
            text = "$label :", // Append colon to the label.
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp)) // Spacer between label and value.

        // Display the value, styled as a link if applicable.
        if (isLink && linkUri != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = OrakniumGold, // Custom theme color for links.
                textDecoration = TextDecoration.Underline, // Underline for link text.
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { uriHandler.openUri(linkUri) } // Open URI on click.
            )
        } else {
            // Display plain text value.
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

/**
 * A composable that displays a clickable social media icon.
 * Clicking the icon opens the provided [url].
 *
 * @param iconResId The drawable resource ID for the social media icon.
 * @param contentDescription A textual description of the icon for accessibility.
 * @param url The URL string to open when the icon is clicked.
 * @param modifier Optional [Modifier] to be applied to the IconButton.
 */
@Composable
fun SocialMediaIcon(
    iconResId: Int,
    contentDescription: String,
    url: String,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current // Used to open URIs.
    IconButton(
        onClick = { uriHandler.openUri(url) }, // Open URL on click.
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = Color.Unspecified, // Use original icon colors, do not apply tint.
            modifier = Modifier.size(40.dp) // Fixed size for the icon.
        )
    }
}


/**
 * Preview composable for the [InfoScreen] in portrait orientation.
 */
@Preview(showBackground = true, name = "Info Screen Portrait")
@Composable
fun InfoScreenPreview() {
    DivinationAppTheme {
        InfoScreen {} // Empty lambda for onNavigateBack in preview.
    }
}

/**
 * Preview composable for the [InfoScreen] in landscape orientation.
 * Demonstrates how the screen might look with different dimensions.
 */
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Info Screen Landscape")
@Composable
fun InfoScreenPreviewLandscape() {
    DivinationAppTheme {
        InfoScreen {} // Empty lambda for onNavigateBack in preview.
    }
}
