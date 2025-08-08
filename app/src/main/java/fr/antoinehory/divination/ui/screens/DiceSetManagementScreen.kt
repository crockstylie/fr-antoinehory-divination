package fr.antoinehory.divination.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// Ensure ContentCopy is imported if DiceSetItemCard uses it directly,
// otherwise this screen doesn't need it directly if it's encapsulated in DiceSetItemCard.
// For the AlertDialog text formatting, `remember` is needed.
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // Required for diceConfigsString in copy dialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.DiceSetItemCard
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.DiceSetViewModel
import fr.antoinehory.divination.viewmodels.DiceSetViewModelFactory

@Composable
fun DiceSetManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateSet: () -> Unit,
    onLaunchSet: (DiceSet) -> Unit,
    onNavigateToEditSet: (diceSetId: String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val diceSetViewModel: DiceSetViewModel = viewModel(
        factory = DiceSetViewModelFactory(application)
    )

    val diceSets by diceSetViewModel.allDiceSets.collectAsState()
    val diceSetToDeleteConfirm by diceSetViewModel.diceSetToDeleteConfirm.collectAsState()
    val diceSetToCopyConfirm by diceSetViewModel.diceSetToCopyConfirm.collectAsState() // For copy dialog

    AppScaffold(
        title = stringResource(id = R.string.dice_set_management_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateSet) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.dice_set_create_new_set_desc))
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
                Text(
                    stringResource(id = R.string.dice_set_no_sets_available),
                    color = OrakniumGold
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(diceSets, key = { it.id }) { diceSet ->
                    DiceSetItemCard(
                        diceSet = diceSet,
                        onLaunch = { onLaunchSet(diceSet) },
                        onToggleFavorite = { diceSetViewModel.toggleFavoriteStatus(diceSet) },
                        onEdit = {
                            onNavigateToEditSet(diceSet.id.toString())
                        },
                        onDelete = { diceSetViewModel.requestDeleteConfirmation(diceSet) },
                        onCopy = { diceSetViewModel.requestCopyConfirmation(diceSet) } // CORRECTLY ADDED HERE
                    )
                }
            }
        }

        // AlertDialog for delete confirmation
        diceSetToDeleteConfirm?.let { setToBeDeleted ->
            AlertDialog(
                onDismissRequest = { diceSetViewModel.cancelDeleteConfirmation() },
                title = { Text(stringResource(R.string.delete_dice_set_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.delete_dice_set_confirmation_message,
                            setToBeDeleted.name
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            diceSetViewModel.deleteDiceSet(setToBeDeleted)
                        }
                    ) {
                        Text(stringResource(R.string.confirm_delete))
                    }
                },
                dismissButton = {
                    Button(onClick = { diceSetViewModel.cancelDeleteConfirmation() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // AlertDialog for copy confirmation
        diceSetToCopyConfirm?.let { setToBeCopied ->
            val diceConfigsString = remember(setToBeCopied.diceConfigs) {
                setToBeCopied.diceConfigs.joinToString(", ") { "${it.count}x ${it.diceType.displayName}" }
            }
            AlertDialog(
                onDismissRequest = { diceSetViewModel.cancelCopyConfirmation() },
                title = { Text(stringResource(R.string.copy_dice_set_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.copy_dice_set_confirmation_message,
                            setToBeCopied.name,
                            diceConfigsString
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            diceSetViewModel.confirmAndCopyDiceSet(setToBeCopied)
                        }
                    ) {
                        Text(stringResource(R.string.confirm_copy))
                    }
                },
                dismissButton = {
                    Button(onClick = { diceSetViewModel.cancelCopyConfirmation() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceSetManagementScreenPreview() {
    val previewSets = listOf(
        DiceSet(
            id = 1,
            name = "Aventure Quotidienne",
            diceConfigs = listOf(
                DiceConfig(diceType = DiceType.D20, count = 1),
                DiceConfig(diceType = DiceType.D6, count = 2)
            ),
            isFavorite = true
        ),
        DiceSet(
            id = 2,
            name = "Dégâts Feu",
            diceConfigs = listOf(
                DiceConfig(diceType = DiceType.D8, count = 3),
                DiceConfig(diceType = DiceType.D4, count = 1)
            )
        ),
        DiceSet(
            id = 3,
            name = "Initiative",
            diceConfigs = listOf(DiceConfig(diceType = DiceType.D20, count = 1))
        )
    )
    DivinationAppTheme {
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
                    DiceSetItemCard(
                        diceSet = diceSet,
                        onLaunch = { },
                        onToggleFavorite = { },
                        onEdit = { },
                        onDelete = { },
                        onCopy = { } // Correctly added for preview
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
                Text("Aucun set de dés disponible.", color = OrakniumGold) // Consider stringResource
            }
        }
    }
}
