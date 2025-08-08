package fr.antoinehory.divination.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.DiceSetViewModel
import fr.antoinehory.divination.viewmodels.DiceSetViewModelFactory

@Composable
fun DiceSetManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateSet: () -> Unit, // Pour la navigation vers l'écran de création
    onLaunchSet: (DiceSet) -> Unit // Pour lancer un set (implémentation future)
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val diceSetViewModel: DiceSetViewModel = viewModel(
        factory = DiceSetViewModelFactory(application)
    )

    val diceSets by diceSetViewModel.allDiceSets.collectAsState()
    // val favoriteDiceSets by diceSetViewModel.favoriteDiceSets.collectAsState() // Si vous voulez un onglet/section favoris

    AppScaffold(
        title = stringResource(id = R.string.dice_set_management_screen_title), // Nouvelle chaîne à ajouter
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateSet) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.dice_set_create_new_set_desc)) // Nouvelle chaîne
            }
        }
    ) { paddingValues ->
        if (diceSets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(stringResource(id = R.string.dice_set_no_sets_available)) // Nouvelle chaîne
                // TODO: Peut-être un bouton ici pour "Créer votre premier set" qui fait la même chose que le FAB
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(diceSets, key = { it.id }) { diceSet ->
                    DiceSetItem(
                        diceSet = diceSet,
                        onLaunch = { onLaunchSet(diceSet) },
                        onToggleFavorite = { diceSetViewModel.toggleFavoriteStatus(diceSet) },
                        onEdit = {
                            // TODO: Naviguer vers l'écran d'édition avec l'ID du set
                            Toast.makeText(context, "Edit: ${diceSet.name}", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { diceSetViewModel.deleteDiceSet(diceSet) }
                    )
                }
            }
        }
    }
}

@Composable
fun DiceSetItem(
    diceSet: DiceSet,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = diceSet.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = diceSet.summaryDisplay, // "2x D6, 1x D20"
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween, // ou End
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (diceSet.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(if (diceSet.isFavorite) R.string.dice_set_remove_from_favorites else R.string.dice_set_add_to_favorites), // Nouvelles chaînes
                        tint = if (diceSet.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f)) // Pousse les boutons d'action à droite
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) { // Taille réduite
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.dice_set_edit_set_desc)) // Nouvelle chaîne
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.dice_set_delete_set_desc)) // Nouvelle chaîne
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onLaunch, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.dice_set_launch_set_desc)) // Nouvelle chaîne
                    }
                }
            }
        }
    }
}

// Preview pour DiceSetManagementScreen
@Preview(showBackground = true)
@Composable
fun DiceSetManagementScreenPreview() {
    val previewSets = listOf(
        DiceSet(id = 1, name = "Aventure Quotidienne", diceConfigs = listOf(DiceConfig(DiceType.D20, 1), DiceConfig(DiceType.D6, 2)), isFavorite = true),
        DiceSet(id = 2, name = "Dégâts Feu", diceConfigs = listOf(DiceConfig(DiceType.D8, 3), DiceConfig(DiceType.D4, 1))),
        DiceSet(id = 3, name = "Initiative", diceConfigs = listOf(DiceConfig(DiceType.D20, 1)))
    )
    // Pour la preview, nous ne pouvons pas instancier directement le ViewModel sans DI ou une fausse implémentation.
    // Nous allons simuler un état où les diceSets sont déjà chargés.
    DivinationAppTheme {
        // AppScaffold simplifié pour la preview, car le viewModel n'est pas disponible.
        AppScaffold(
            title = "Gérer les Sets (Aperçu)",
            canNavigateBack = true,
            onNavigateBack = {},
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Filled.Add, contentDescription = "Créer un set")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(previewSets, key = { it.id }) { diceSet ->
                    DiceSetItem(
                        diceSet = diceSet,
                        onLaunch = { },
                        onToggleFavorite = { },
                        onEdit = { },
                        onDelete = { }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DiceSetManagementScreen - No Sets")
@Composable
fun DiceSetManagementScreen_NoSets_Preview() {
    DivinationAppTheme {
        AppScaffold(
            title = "Gérer les Sets (Vide)",
            canNavigateBack = true,
            onNavigateBack = {},
            floatingActionButton = {
                FloatingActionButton(onClick = {}) {
                    Icon(Icons.Filled.Add, contentDescription = "Créer un set")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Aucun set de dés disponible.")
            }
        }
    }
}
