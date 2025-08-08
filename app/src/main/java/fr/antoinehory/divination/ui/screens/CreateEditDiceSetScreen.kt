package fr.antoinehory.divination.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModel
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModelFactory

@Composable
fun CreateEditDiceSetScreen(
    onNavigateBack: () -> Unit,
    diceSetId: String? // Null pour la création, non-null pour l'édition
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Convertir l'ID de String? en Long? pour le ViewModel
    val diceSetIdLong = diceSetId?.toLongOrNull()

    val viewModel: CreateEditDiceSetViewModel = viewModel(
        factory = CreateEditDiceSetViewModelFactory(application, diceSetIdLong)
    )

    val setName by viewModel.setName.collectAsState()
    // val diceConfigs by viewModel.diceConfigs.collectAsState() // Sera utilisé plus tard

    val screenTitle = if (diceSetIdLong == null) {
        stringResource(id = R.string.create_dice_set_screen_title)
    } else {
        stringResource(id = R.string.edit_dice_set_screen_title)
    }

    AppScaffold(
        title = screenTitle,
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = {
                viewModel.saveDiceSet(onSuccess = {
                    onNavigateBack() // Retour à l'écran précédent après sauvegarde
                })
            }) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = stringResource(R.string.save_dice_set_desc) // Nouvelle chaîne
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // verticalArrangement = Arrangement.Center // On va commencer par le haut maintenant
        ) {
            OutlinedTextField(
                value = setName,
                onValueChange = { viewModel.updateSetName(it) },
                label = { Text(stringResource(R.string.dice_set_name_label)) }, // Nouvelle chaîne
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("TODO: Liste des dés et gestion des configurations de dés")

            // Ici viendra la liste des DiceConfig et les boutons pour les gérer
        }
    }
}

// Nouvelle chaîne à ajouter dans strings.xml
// <string name="save_dice_set_desc">Save Dice Set</string>
// <string name="dice_set_name_label">Set Name</string>

@Preview(showBackground = true, name = "Create Dice Set Preview")
@Composable
fun CreateDiceSetScreenPreview() {
    DivinationAppTheme {
        CreateEditDiceSetScreen(onNavigateBack = {}, diceSetId = null)
    }
}

@Preview(showBackground = true, name = "Edit Dice Set Preview")
@Composable
fun EditDiceSetScreenPreview() {
    DivinationAppTheme {
        CreateEditDiceSetScreen(onNavigateBack = {}, diceSetId = "123")
    }
}

