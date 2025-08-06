package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.* // Maintenu pour les composants existants
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // AJOUTÉ
import androidx.compose.ui.platform.LocalContext // AJOUTÉ
import androidx.compose.ui.res.stringResource // AJOUTÉ
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R // AJOUTÉ
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.data.InteractionPreferences

// AJOUTS pour le nouveau ViewModel et le dialogue :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.SettingsViewModel
import fr.antoinehory.divination.viewmodels.SettingsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    // AJOUT: Initialisation du SettingsViewModel
    settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModelFactory(
            (LocalContext.current.applicationContext as DivinationApplication).launchLogRepository
        )
    )
) {
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    // AJOUT: État pour le dialogue de confirmation
    val showClearStatsDialog by settingsViewModel.showClearStatsConfirmationDialog.collectAsState()

    // AJOUT: Dialogue de confirmation
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
        title = stringResource(id = R.string.settings_screen_title), // MODIFIÉ: Titre générique
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }

            // AJOUT: Section pour vider les statistiques
            Divider(modifier = Modifier.padding(vertical = 16.dp)) // Séparateur visuel

            Text(
                stringResource(id = R.string.settings_data_management_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SettingTextItem( // Remplacé par un TextItem simple pour l'action
                title = stringResource(id = R.string.settings_clear_stats_button),
                onClick = { settingsViewModel.onClearStatsClicked() },
                textColor = MaterialTheme.colorScheme.error // Couleur pour indiquer une action potentiellement destructive
            )
        }
    }
}

// MODIFIÉ: SettingRadioItem pour cohérence (pas de changement majeur ici, juste pour revue)
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

// AJOUT: Un composable simple pour les options cliquables qui ne sont pas des RadioButton
@Composable
fun SettingTextItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified // Permet de spécifier une couleur pour le texte
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


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DivinationAppTheme {
        // Le preview ne pourra pas instancier le ViewModel sans une fausse implémentation
        // ou des ajustements plus complexes.
        // On peut prévisualiser la structure de base.
        SettingsScreen(onNavigateBack = {})
    }
}

