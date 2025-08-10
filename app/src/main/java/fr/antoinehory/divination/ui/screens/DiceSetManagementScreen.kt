package fr.antoinehory.divination.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.DiceSetItemCard
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.data.repository.UserPreferencesRepository
import fr.antoinehory.divination.viewmodels.DiceSetViewModel
import fr.antoinehory.divination.viewmodels.DiceSetViewModelFactory

/**
 * Composable screen for managing user-created dice sets.
 * This screen displays a list of dice sets, allowing users to create, edit, delete,
 * copy, favorite, and launch (set as active) dice sets.
 * It uses [DiceSetViewModel] to handle the business logic and data operations.
 *
 * @param onNavigateBack Callback invoked when the user navigates back from this screen.
 * @param onNavigateToCreateSet Callback invoked when the user taps the FloatingActionButton to create a new dice set.
 * @param onLaunchSet Callback invoked when a dice set is confirmed to be launched (set as active).
 *                    The [DiceSet] to be launched is passed as a parameter.
 * @param onNavigateToEditSet Callback invoked when the user chooses to edit a dice set.
 *                            The ID of the dice set to edit is passed as a [String].
 */
@Composable
fun DiceSetManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateSet: () -> Unit,
    onLaunchSet: (DiceSet) -> Unit,
    onNavigateToEditSet: (diceSetId: String) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Repository for user preferences, used by the ViewModel.
    val userPreferencesRepository = UserPreferencesRepository(context.applicationContext)

    // ViewModel for managing dice set data and operations.
    val diceSetViewModel: DiceSetViewModel = viewModel(
        factory = DiceSetViewModelFactory(application, userPreferencesRepository)
    )

    // Collecting state from the DiceSetViewModel.
    val diceSets by diceSetViewModel.allDiceSets.collectAsState()
    val diceSetToDeleteConfirm by diceSetViewModel.diceSetToDeleteConfirm.collectAsState() // DiceSet pending delete confirmation.
    val diceSetToCopyConfirm by diceSetViewModel.diceSetToCopyConfirm.collectAsState()     // DiceSet pending copy confirmation.
    val diceSetToSetActiveConfirm by diceSetViewModel.diceSetToSetActiveConfirm.collectAsState() // DiceSet pending activation confirmation.

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
        // Display a message if no dice sets are available.
        if (diceSets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp), // Additional padding for content.
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(id = R.string.dice_set_no_sets_available),
                    color = MaterialTheme.colorScheme.primary // MODIFIÉ
                )
            }
        } else {
            // Display the list of dice sets.
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Padding at the bottom to avoid FAB overlap.
            ) {
                items(diceSets, key = { it.id }) { diceSet ->
                    DiceSetItemCard(
                        diceSet = diceSet,
                        onLaunch = { diceSetViewModel.requestSetActiveConfirmation(diceSet) },
                        onToggleFavorite = { diceSetViewModel.toggleFavoriteStatus(diceSet) },
                        onEdit = { onNavigateToEditSet(diceSet.id.toString()) },
                        onDelete = { diceSetViewModel.requestDeleteConfirmation(diceSet) },
                        onCopy = { diceSetViewModel.requestCopyConfirmation(diceSet) }
                    )
                }
            }
        }

        // Confirmation dialog for deleting a dice set.
        diceSetToDeleteConfirm?.let { setToBeDeleted ->
            AlertDialog(
                onDismissRequest = { diceSetViewModel.cancelDeleteConfirmation() },
                title = { Text(stringResource(R.string.delete_dice_set_confirmation_title)) },
                text = { Text(stringResource(R.string.delete_dice_set_confirmation_message, setToBeDeleted.name)) },
                confirmButton = {
                    Button(onClick = { diceSetViewModel.deleteDiceSet(setToBeDeleted) }) {
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

        // Confirmation dialog for copying a dice set.
        diceSetToCopyConfirm?.let { setToBeCopied ->
            // Memoize the string representation of dice configurations for the dialog.
            val diceConfigsString = remember(setToBeCopied.diceConfigs) {
                setToBeCopied.diceConfigs.joinToString(", ") { "${it.count}x ${it.diceType.displayName}" }
            }
            AlertDialog(
                onDismissRequest = { diceSetViewModel.cancelCopyConfirmation() },
                title = { Text(stringResource(R.string.copy_dice_set_confirmation_title)) },
                text = { Text(stringResource(R.string.copy_dice_set_confirmation_message, setToBeCopied.name, diceConfigsString)) },
                confirmButton = {
                    Button(onClick = { diceSetViewModel.confirmAndCopyDiceSet(setToBeCopied) }) {
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

        // Confirmation dialog for setting a dice set as active (launching).
        diceSetToSetActiveConfirm?.let { setToBeActivated ->
            AlertDialog(
                onDismissRequest = { diceSetViewModel.cancelSetActiveConfirmation() },
                title = { Text(stringResource(R.string.set_active_dice_set_confirmation_title)) },
                text = {
                    Text(
                        stringResource(
                            R.string.set_active_dice_set_confirmation_message,
                            setToBeActivated.name
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            diceSetViewModel.confirmSetActiveDiceSet(setToBeActivated)
                            onLaunchSet(setToBeActivated) // Propagate launch event.
                        }
                    ) {
                        Text(stringResource(R.string.confirm_set_active))
                    }
                },
                dismissButton = {
                    Button(onClick = { diceSetViewModel.cancelSetActiveConfirmation() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

/**
 * Preview composable for the [DiceSetManagementScreen].
 * This preview displays the screen with a sample list of dice sets.
 */
@Preview(showBackground = true)
@Composable
fun DiceSetManagementScreenPreview() {
    // Sample data for the preview.
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
        // AppScaffold is used directly here as the ViewModel interactions are mocked.
        AppScaffold(
            title = "Gérer les Sets (Aperçu)", // Preview-specific title.
            canNavigateBack = true,
            onNavigateBack = {}, // No-op for preview.
            floatingActionButton = {
                FloatingActionButton(onClick = {}) { // No-op FAB for preview.
                    Icon(Icons.Filled.Add, contentDescription = "Créer un set")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(previewSets, key = { it.id }) { diceSet ->
                    DiceSetItemCard(
                        diceSet = diceSet,
                        onLaunch = { }, // No-op actions for preview.
                        onToggleFavorite = { },
                        onEdit = { },
                        onDelete = { },
                        onCopy = { }
                    )
                }
            }
        }
    }
}

/**
 * Preview composable for the [DiceSetManagementScreen] in its empty state.
 * This preview displays the screen when no dice sets are available.
 */
@Preview(showBackground = true, name = "DiceSetManagementScreen - Empty")
@Composable
fun DiceSetManagementScreenEmptyPreview() {
    DivinationAppTheme {
        AppScaffold(
            title = "Gérer les Sets (Vide - Aperçu)",
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
                Text(
                    stringResource(id = R.string.dice_set_no_sets_available),
                    color = MaterialTheme.colorScheme.primary // MODIFIÉ (Aussi dans le preview pour cohérence)
                )
            }
        }
    }
}

