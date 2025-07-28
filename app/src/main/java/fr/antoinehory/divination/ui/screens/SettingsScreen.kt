package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.data.InteractionMode // Importer l'enum
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
// InteractionPreferences a changé, mais l'import reste
import fr.antoinehory.divination.data.InteractionPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    AppScaffold(
        title = "Mode d'Interaction", // Titre plus concis
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
                "Choisissez comment lancer une action :", // Texte d'instruction
                style = MaterialTheme.typography.titleMedium, // Style de texte ajusté
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val interactionOptions = listOf(
                InteractionMode.TAP to "Taper pour lancer",
                InteractionMode.SHAKE to "Secouer pour lancer"
            )

            interactionOptions.forEach { (mode, label) ->
                val isEnabled = when (mode) {
                    InteractionMode.SHAKE -> isShakeAvailable
                    InteractionMode.TAP -> true // Toujours disponible
                }
                SettingRadioItem(
                    title = label,
                    selected = interactionPrefs.activeInteractionMode == mode,
                    onClick = {
                        if (isEnabled) { // Ne change que si l'option est activable
                            interactionViewModel.setActiveInteractionMode(mode)
                        }
                    },
                    enabled = isEnabled
                )
            }

            // Afficher un message si l'option "Secouer" n'est pas disponible
            // et que l'utilisateur pourrait essayer de la sélectionner (même si on la désactive)
            if (!isShakeAvailable) {
                Text(
                    "Le mode \"Secouer\" n'est pas disponible (pas d'accéléromètre).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // Couleur moins agressive que error
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }
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
            .padding(vertical = 12.dp), // Augmenter le padding vertical pour une meilleure zone cliquable
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // L'action de clic est gérée par le Row
            enabled = enabled,
            modifier = Modifier.size(24.dp) // Taille du RadioButton
        )
        Spacer(Modifier.width(16.dp)) // Espace entre RadioButton et Text
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge, // Garder bodyLarge ou ajuster si nécessaire
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.weight(1f) // Permet au texte de prendre l'espace restant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DivinationAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingRadioItem(title = "Taper pour lancer", selected = true, onClick = {}, enabled = true)
            SettingRadioItem(title = "Secouer pour lancer", selected = false, onClick = {}, enabled = true)
            SettingRadioItem(title = "Secouer pour lancer (non dispo)", selected = false, onClick = {}, enabled = false)
            Text(
                "Le mode \"Secouer\" n'est pas disponible (pas d'accéléromètre).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )
        }
    }
}
