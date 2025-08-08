package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // AJOUT
import androidx.compose.foundation.verticalScroll // AJOUT
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
// AJOUTS pour le nouveau ViewModel et le dialogue :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.SettingsViewModel
import fr.antoinehory.divination.viewmodels.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (LocalContext.current.applicationContext as DivinationApplication).launchLogRepository
        )
    )
) {
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()
    val showClearStatsDialog by settingsViewModel.showClearStatsConfirmationDialog.collectAsState()

    if (showClearStatsDialog) {
        AlertDialog(
            onDismissRequest = { settingsViewModel.onDismissClearStatsDialog() },
            title = { Text(stringResource(id = R.string.settings_clear_stats_dialog_title)) },
            text = { Text(stringResource(id = R.string.settings_clear_stats_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { settingsViewModel.onConfirmClearStats() }) {
                    Text(stringResource(id = R.string.settings_clear_stats_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { settingsViewModel.onDismissClearStatsDialog() }) {
                    Text(stringResource(id = R.string.settings_clear_stats_dialog_cancel))
                }
            }
        )
    }

    AppScaffold(
        title = stringResource(id = R.string.settings_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding du Scaffold
                .verticalScroll(rememberScrollState()) // Rend la colonne défilable
                .padding(horizontal = 16.dp, vertical = 8.dp) // Padding interne pour le contenu
        ) {
            Text(
                stringResource(id = R.string.settings_interaction_mode_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val interactionOptions = listOf(
                InteractionMode.TAP to stringResource(R.string.settings_interaction_mode_tap),
                InteractionMode.SHAKE to stringResource(R.string.settings_interaction_mode_shake)
            )

            interactionOptions.forEach { (mode, label) ->
                val isEnabled = when (mode) {
                    InteractionMode.SHAKE -> isShakeAvailable
                    InteractionMode.TAP -> true
                }
                SettingRadioItem(
                    title = label,
                    selected = interactionPrefs.activeInteractionMode == mode,
                    onClick = {
                        if (isEnabled) {
                            interactionViewModel.setActiveInteractionMode(mode)
                        }
                    },
                    enabled = isEnabled
                )
            }

            if (!isShakeAvailable) {
                Text(
                    stringResource(id = R.string.settings_interaction_mode_shake_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp) // Ajout d'un padding bottom
                )
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                stringResource(id = R.string.settings_data_management_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingTextItem(
                title = stringResource(id = R.string.settings_clear_stats_button),
                onClick = { settingsViewModel.onClearStatsClicked() },
                textColor = MaterialTheme.colorScheme.error
            )

            // Ajout d'un Spacer en bas pour s'assurer que le dernier élément est bien visible
            // lors du défilement, surtout si un clavier virtuel apparaîtrait (peu probable ici mais bonne pratique)
            // ou si le contenu était plus long.
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingRadioItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SettingTextItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, role = Role.Button)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen Portrait")
@Composable
fun SettingsScreenPreview() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "Settings Screen Landscape")
@Composable
fun SettingsScreenPreviewLandscape() {
    DivinationAppTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
