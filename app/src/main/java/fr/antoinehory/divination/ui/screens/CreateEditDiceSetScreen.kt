package fr.antoinehory.divination.ui.screens

import android.app.Application
// import android.util.Log // RETIRÉ car plus de Log.d dans cette fonction
import android.widget.Toast
import androidx.compose.foundation.BorderStroke // AJOUTÉ si absent
import androidx.compose.foundation.background // AJOUTÉ si absent
import androidx.compose.foundation.border // AJOUTÉ si absent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues // AJOUTÉ si absent (pour le DropdownMenuItem)
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.heightIn // Peut-être plus nécessaire
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumBackground // AJOUTÉ si absent
import fr.antoinehory.divination.ui.theme.OrakniumGold // AJOUTÉ si absent
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModel
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
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

    val currentSetName by viewModel.setName.collectAsState()
    val diceConfigs by viewModel.diceConfigs.collectAsState()
    val saveErrorId by viewModel.saveError.collectAsState()

    val screenTitle = if (diceSetIdLong == null) {
        stringResource(id = R.string.create_dice_set_screen_title)
    } else {
        stringResource(id = R.string.edit_dice_set_screen_title)
    }
    val defaultNewSetName = stringResource(id = R.string.default_new_dice_set_name)

    var setNameFieldValue by remember(currentSetName, defaultNewSetName) {
        mutableStateOf(TextFieldValue(currentSetName, TextRange(currentSetName.length)))
    }
    var selectAllOnNextFocus by remember { mutableStateOf(diceSetIdLong == null && currentSetName == defaultNewSetName) }

    var showAddDiceSection by remember { mutableStateOf(false) }
    var selectedDiceType by remember { mutableStateOf(DiceType.D6) }
    var selectedDiceCountString by remember { mutableStateOf("1") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var editingConfigIndex by remember { mutableStateOf<Int?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentSetName, defaultNewSetName) {
        if (currentSetName != setNameFieldValue.text) {
            setNameFieldValue = TextFieldValue(currentSetName, TextRange(currentSetName.length))
            if (diceSetIdLong == null && currentSetName == defaultNewSetName) {
                selectAllOnNextFocus = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(screenTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back_desc))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveDiceSet(onSuccess = { onNavigateBack() })
                    }) {
                        Icon(Icons.Filled.Done, stringResource(R.string.save_dice_set_desc))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = setNameFieldValue,
                onValueChange = {
                    setNameFieldValue = it
                    viewModel.updateSetName(it.text)
                    if (selectAllOnNextFocus && it.text != defaultNewSetName) {
                        selectAllOnNextFocus = false
                    }
                },
                label = { Text(stringResource(R.string.dice_set_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused && selectAllOnNextFocus && setNameFieldValue.text == defaultNewSetName) {
                            setNameFieldValue = setNameFieldValue.copy(
                                selection = TextRange(0, setNameFieldValue.text.length)
                            )
                            selectAllOnNextFocus = false
                        }
                    },
                singleLine = true,
                isError = saveErrorId == R.string.error_set_name_empty
            )
            if (saveErrorId == R.string.error_set_name_empty) {
                Text(
                    text = stringResource(id = R.string.error_set_name_empty),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

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
                Button(onClick = {
                    val wasEditing = editingConfigIndex != null
                    showAddDiceSection = !showAddDiceSection
                    if (!showAddDiceSection || wasEditing) {
                        editingConfigIndex = null
                        selectedDiceType = DiceType.D6
                        selectedDiceCountString = "1"
                        dropdownExpanded = false
                    }
                    if (showAddDiceSection && !wasEditing && editingConfigIndex == null) {
                        selectedDiceType = DiceType.D6
                        selectedDiceCountString = "1"
                        dropdownExpanded = false
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_dice_config_desc))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showAddDiceSection) stringResource(R.string.cancel) // MODIFIED HERE
                        else stringResource(R.string.add_dice_button)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (showAddDiceSection) {
                AddDiceConfigInputSection(
                    selectedDiceType = selectedDiceType,
                    onDiceTypeChange = { newType -> selectedDiceType = newType },
                    isDropdownExpanded = dropdownExpanded,
                    onDropdownExpandedChange = { expanded -> dropdownExpanded = expanded },
                    selectedDiceCountString = selectedDiceCountString,
                    onDiceCountChange = { if (it.isEmpty() || it.all { char -> char.isDigit() }) selectedDiceCountString = it },
                    isEditing = editingConfigIndex != null,
                    onConfirmClick = {
                        val count = selectedDiceCountString.toIntOrNull()
                        if (count != null && count > 0) {
                            if (editingConfigIndex != null) {
                                viewModel.updateDiceConfig(editingConfigIndex!!, selectedDiceType, count)
                            } else {
                                viewModel.addDiceConfig(DiceConfig(diceType = selectedDiceType, count = count))
                            }
                            selectedDiceType = DiceType.D6
                            selectedDiceCountString = "1"
                            showAddDiceSection = false
                            editingConfigIndex = null
                            dropdownExpanded = false
                        } else {
                            Toast.makeText(context, context.getString(R.string.error_invalid_dice_count), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (diceConfigs.isEmpty()) {
                Text(
                    stringResource(R.string.no_dice_configs_yet) +
                            if (saveErrorId == R.string.error_no_dice_configs) " " + stringResource(R.string.error_no_dice_configs_suffix) else "",
                    color = if (saveErrorId == R.string.error_no_dice_configs) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(diceConfigs, key = { index, item -> item.id }) { index, config ->
                        DiceConfigRow(
                            diceConfig = config,
                            isCurrentlyEditing = editingConfigIndex == index,
                            onClick = {
                                editingConfigIndex = index
                                selectedDiceType = config.diceType
                                selectedDiceCountString = config.count.toString()
                                showAddDiceSection = true
                                dropdownExpanded = false
                            },
                            onIncrement = { viewModel.incrementDiceCount(index) },
                            onDecrement = { viewModel.decrementDiceCount(index) },
                            onRemove = { viewModel.removeDiceConfig(index) }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDiceConfigInputSection(
    selectedDiceType: DiceType,
    onDiceTypeChange: (DiceType) -> Unit,
    isDropdownExpanded: Boolean,
    onDropdownExpandedChange: (Boolean) -> Unit,
    selectedDiceCountString: String,
    onDiceCountChange: (String) -> Unit,
    isEditing: Boolean,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Log de débogage retiré
                    onDropdownExpandedChange(!isDropdownExpanded)
                }
        ) {
            OutlinedTextField(
                value = selectedDiceType.displayName,
                onValueChange = { /* Lecture seule */ },
                label = { Text(stringResource(R.string.dice_type_label)) },
                readOnly = true,
                enabled = false, // RESTE false pour que le Box reçoive le clic
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select dice type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                colors = TextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent, 
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline, 
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant, 
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { onDropdownExpandedChange(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fr.antoinehory.divination.ui.theme.OrakniumBackground) // Votre couleur de fond
                    .border(BorderStroke(1.dp, fr.antoinehory.divination.ui.theme.OrakniumGold)) // Votre couleur de bordure
            ) {
                DiceType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                type.displayName,
                                style = MaterialTheme.typography.bodyLarge // Texte plus gros
                            )
                        },
                        onClick = {
                            onDiceTypeChange(type)
                            onDropdownExpandedChange(false)
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp) // Marge interne pour les items
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = selectedDiceCountString,
            onValueChange = onDiceCountChange,
            label = { Text(stringResource(R.string.quantity_label)) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onConfirmClick,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                stringResource(
                    if (isEditing) R.string.update_button
                    else R.string.confirm_add_to_set_button
                )
            )
        }
    }
}

@Composable
fun DiceConfigRow(
    diceConfig: DiceConfig,
    isCurrentlyEditing: Boolean,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isCurrentlyEditing) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = diceConfig.diceType.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = diceConfig.count > 1) {
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.decrement_dice_count_desc))
            }
            Text(
                text = diceConfig.count.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onIncrement) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.increment_dice_count_desc))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, stringResource(R.string.remove_dice_config_desc))
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Create New Dice Set Preview")
@Composable
fun CreateEditDiceSetScreenNewSetPreview() {
    val application = LocalContext.current.applicationContext as Application
    val previewViewModel = CreateEditDiceSetViewModelFactory(application, null).create(CreateEditDiceSetViewModel::class.java)

    DivinationAppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val screenTitle = "Create Dice Set (Preview)"
        val defaultNewSetNameInPreview = stringResource(id = R.string.default_new_dice_set_name)

        val currentSetNameFromViewModel by previewViewModel.setName.collectAsState()
        var setNameFieldValueInPreview by remember(currentSetNameFromViewModel, defaultNewSetNameInPreview) {
            mutableStateOf(TextFieldValue(currentSetNameFromViewModel, TextRange(currentSetNameFromViewModel.length)))
        }
        var selectAllOnNextFocusInPreview by remember { mutableStateOf(true) }


        val diceConfigsFromVM by previewViewModel.diceConfigs.collectAsState()
        var editingConfigIndexInPreview by remember { mutableStateOf<Int?>(if (diceConfigsFromVM.isNotEmpty()) 0 else null) }
        var showAddSectionInPreview by remember { mutableStateOf(editingConfigIndexInPreview != null) }

        var selectedDiceTypeInPreview by remember { mutableStateOf(if (editingConfigIndexInPreview != null && diceConfigsFromVM.isNotEmpty()) diceConfigsFromVM[0].diceType else DiceType.D6) }
        var selectedDiceCountInPreview by remember { mutableStateOf(if (editingConfigIndexInPreview != null && diceConfigsFromVM.isNotEmpty()) diceConfigsFromVM[0].count.toString() else "1") }
        var dropdownExpandedInPreview by remember { mutableStateOf(false) }


        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { /* Do nothing in preview */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Do nothing in preview */ }) {
                            Icon(Icons.Filled.Done, "Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                OutlinedTextField(
                    value = setNameFieldValueInPreview,
                    onValueChange = {
                        setNameFieldValueInPreview = it
                        if (selectAllOnNextFocusInPreview && it.text != defaultNewSetNameInPreview) {
                            selectAllOnNextFocusInPreview = false
                        }
                    },
                    label = { Text("Set Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused && selectAllOnNextFocusInPreview && setNameFieldValueInPreview.text == defaultNewSetNameInPreview) {
                                setNameFieldValueInPreview = setNameFieldValueInPreview.copy(
                                    selection = TextRange(0, setNameFieldValueInPreview.text.length)
                                )
                                selectAllOnNextFocusInPreview = false
                            }
                        },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dice Configurations", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = {
                        val wasEditing = editingConfigIndexInPreview != null
                        showAddSectionInPreview = !showAddSectionInPreview
                        if (!showAddSectionInPreview || wasEditing) {
                            editingConfigIndexInPreview = null
                            selectedDiceTypeInPreview = DiceType.D6
                            selectedDiceCountInPreview = "1"
                        }
                        if (showAddSectionInPreview && !wasEditing && editingConfigIndexInPreview == null) {
                            selectedDiceTypeInPreview = DiceType.D6
                            selectedDiceCountInPreview = "1"
                        }
                        dropdownExpandedInPreview = false
                    }) {
                        Icon(Icons.Filled.Add, "Add/Cancel"); Spacer(modifier = Modifier.width(4.dp)); Text(if (showAddSectionInPreview) "Cancel" else "Add Die")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (showAddSectionInPreview) {
                    AddDiceConfigInputSection(
                        selectedDiceType = selectedDiceTypeInPreview,
                        onDiceTypeChange = { selectedDiceTypeInPreview = it},
                        isDropdownExpanded = dropdownExpandedInPreview,
                        onDropdownExpandedChange = { expanded ->
                            // Log.d("DiceDebugPreview", "Preview AddDiceConfigInputSection: onDropdownExpandedChange called with $expanded") // Optionnel
                            dropdownExpandedInPreview = expanded
                        },
                        selectedDiceCountString = selectedDiceCountInPreview,
                        onDiceCountChange = { selectedDiceCountInPreview = it },
                        isEditing = editingConfigIndexInPreview != null,
                        onConfirmClick = {
                            val count = selectedDiceCountInPreview.toIntOrNull()
                            if (count != null && count > 0) {
                                if (editingConfigIndexInPreview != null) {
                                    previewViewModel.updateDiceConfig(editingConfigIndexInPreview!!,selectedDiceTypeInPreview, count)
                                } else {
                                    previewViewModel.addDiceConfig(DiceConfig(diceType = selectedDiceTypeInPreview, count = count))
                                }
                                showAddSectionInPreview = false
                                editingConfigIndexInPreview = null
                                dropdownExpandedInPreview = false
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(diceConfigsFromVM, key = { index, item -> item.id }) { index, config ->
                        DiceConfigRow(
                            diceConfig = config,
                            isCurrentlyEditing = editingConfigIndexInPreview == index,
                            onClick = {
                                editingConfigIndexInPreview = index
                                selectedDiceTypeInPreview = config.diceType
                                selectedDiceCountInPreview = config.count.toString()
                                showAddSectionInPreview = true
                                dropdownExpandedInPreview = false
                            },
                            onIncrement = { previewViewModel.incrementDiceCount(index) },
                            onDecrement = { previewViewModel.decrementDiceCount(index) },
                            onRemove = { previewViewModel.removeDiceConfig(index) }
                        )
                    }
                }
            }
        }
    }
}
