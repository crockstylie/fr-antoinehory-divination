package fr.antoinehory.divination.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable // Pour le DropdownMenu
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Pour le DropdownMenu
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions // Pour le champ de quantité
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown // Pour le DropdownMenu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
// OutlinedButton n'est pas utilisé, je le retire pour nettoyer les imports.
// import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType // Pour le champ de quantité
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig // Assurez-vous que cette classe a un champ 'id'
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModel
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModelFactory

@Composable
fun CreateEditDiceSetScreen(
    onNavigateBack: () -> Unit,
    diceSetId: String?
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val diceSetIdLong = diceSetId?.toLongOrNull()
    val viewModel: CreateEditDiceSetViewModel = viewModel(
        factory = CreateEditDiceSetViewModelFactory(application, diceSetIdLong)
    )

    val setName by viewModel.setName.collectAsState()
    val diceConfigs by viewModel.diceConfigs.collectAsState()

    val screenTitle = if (diceSetIdLong == null) {
        stringResource(id = R.string.create_dice_set_screen_title)
    } else {
        stringResource(id = R.string.edit_dice_set_screen_title)
    }

    // États pour la section d'ajout de dé
    var showAddDiceSection by remember { mutableStateOf(false) }
    var selectedDiceType by remember { mutableStateOf(DiceType.D6) } // Défaut D6
    var selectedDiceCountString by remember { mutableStateOf("1") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AppScaffold(
        title = screenTitle,
        canNavigateBack = true,
        onNavigateBack = onNavigateBack,
        actions = {
            IconButton(onClick = {
                viewModel.saveDiceSet(onSuccess = { onNavigateBack() })
            }) {
                Icon(Icons.Filled.Done, stringResource(R.string.save_dice_set_desc))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = setName,
                onValueChange = { viewModel.updateSetName(it) },
                label = { Text(stringResource(R.string.dice_set_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.dice_configurations_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = { showAddDiceSection = !showAddDiceSection }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_dice_config_desc))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showAddDiceSection) stringResource(R.string.cancel_add_dice_button) else stringResource(R.string.add_dice_button))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (showAddDiceSection) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedDiceType.displayName,
                            onValueChange = { },
                            label = { Text(stringResource(R.string.dice_type_label)) },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select dice type") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DiceType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.displayName) },
                                    onClick = {
                                        selectedDiceType = type
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = selectedDiceCountString,
                        onValueChange = { if (it.all { char -> char.isDigit() }) selectedDiceCountString = it },
                        label = { Text(stringResource(R.string.quantity_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val count = selectedDiceCountString.toIntOrNull()
                            if (count != null && count > 0) {
                                // DiceConfig() générera un ID unique si la data class est modifiée correctement
                                viewModel.addDiceConfig(DiceConfig(diceType = selectedDiceType, count = count))
                                selectedDiceType = DiceType.D6
                                selectedDiceCountString = "1"
                                showAddDiceSection = false
                            } else {
                                // TODO: Gérer l'erreur de quantité invalide (par ex. Toast ou message d'erreur)
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.confirm_add_to_set_button))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (diceConfigs.isEmpty()) {
                Text(stringResource(R.string.no_dice_configs_yet))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // MODIFICATION ICI: Utilisation de item.id comme clé
                    itemsIndexed(diceConfigs, key = { _, item -> item.id }) { index, config ->
                        DiceConfigRow(
                            diceConfig = config,
                            onRemove = { viewModel.removeDiceConfig(index) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DiceConfigRow(
    diceConfig: DiceConfig,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${diceConfig.count} x ${diceConfig.diceType.displayName}",
            style = MaterialTheme.typography.bodyLarge
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Filled.Delete, stringResource(R.string.remove_dice_config_desc))
        }
    }
}

@Preview(showBackground = true, name = "Create Dice Set Screen Preview - Add Section")
@Composable
fun CreateEditDiceSetScreenWithAddSectionPreview() {
    val application = LocalContext.current.applicationContext as Application
    val previewViewModel = CreateEditDiceSetViewModelFactory(application, null).create(CreateEditDiceSetViewModel::class.java)
    previewViewModel.updateSetName("Mon Set de Test")
    // Ces appels créeront des DiceConfig avec des ID uniques si la data class a été modifiée.
    previewViewModel.addDiceConfig(DiceConfig(diceType = DiceType.D20, count = 1))
    previewViewModel.addDiceConfig(DiceConfig(diceType = DiceType.D6, count = 2))


    DivinationAppTheme {
        val setName by previewViewModel.setName.collectAsState()
        val diceConfigs by previewViewModel.diceConfigs.collectAsState()

        var showAddDiceSection by remember { mutableStateOf(true) }
        var selectedDiceType by remember { mutableStateOf(DiceType.D6) }
        var selectedDiceCountString by remember { mutableStateOf("1") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AppScaffold(
            title = "Create Dice Set",
            canNavigateBack = true,
            onNavigateBack = {},
            actions = { IconButton(onClick = {}) { Icon(Icons.Filled.Done, "Save") } }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                OutlinedTextField(value = setName, onValueChange = {}, label = { Text("Set Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dice Configurations", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showAddDiceSection = !showAddDiceSection }) {
                        Icon(Icons.Filled.Add, "Add/Cancel"); Spacer(modifier = Modifier.width(4.dp)); Text(if (showAddDiceSection) "Cancel" else "Add Die")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (showAddDiceSection) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = selectedDiceType.displayName, onValueChange = { },label = { Text("Dice Type") }, readOnly = true, trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select") }, modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true })
                            DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }, modifier = Modifier.fillMaxWidth()) {
                                DiceType.values().forEach { type ->
                                    DropdownMenuItem(text = { Text(type.displayName) }, onClick = { selectedDiceType = type; dropdownExpanded = false })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = selectedDiceCountString, onValueChange = { selectedDiceCountString = it }, label = { Text("Quantity") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val count = selectedDiceCountString.toIntOrNull()
                            if (count != null && count > 0) {
                                previewViewModel.addDiceConfig(DiceConfig(diceType = selectedDiceType, count = count))
                                // Réinitialiser pour la prochaine fois et masquer
                                selectedDiceType = DiceType.D6
                                selectedDiceCountString = "1"
                                // showAddDiceSection = false // Optionnel dans la preview de juste montrer la section
                            }
                        }, modifier = Modifier.align(Alignment.End)) { Text("Add to Set") }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                if (diceConfigs.isEmpty()) {
                    Text("No dice configured yet.")
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        // MODIFICATION ICI: Utilisation de item.id comme clé également pour la preview
                        itemsIndexed(diceConfigs, key = { _, item -> item.id }) { index, config ->
                            DiceConfigRow(diceConfig = config, onRemove = {
                                // Simuler la suppression dans la preview si nécessaire
                                // previewViewModel.removeDiceConfig(index) // Décommenter si implémenté et souhaité
                            })
                        }
                    }
                }
            }
        }
    }
}