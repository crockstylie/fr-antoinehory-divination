package fr.antoinehory.divination.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModel
import fr.antoinehory.divination.viewmodels.CreateEditDiceSetViewModelFactory

/**
 * Maximum allowed count for a single type of dice in a configuration.
 */
const val MAX_DICE_COUNT = 1000

/**
 * Composable screen for creating a new dice set or editing an existing one.
 * Users can define a name for the set and add multiple dice configurations,
 * specifying the type of dice (e.g., D6, D20) and the count for each type.
 *
 * @param onNavigateBack Callback invoked when the user navigates back from this screen.
 * @param diceSetId The ID of the dice set to be edited. If `null`, the screen operates in "create new set" mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditDiceSetScreen(
    onNavigateBack: () -> Unit,
    diceSetId: String?
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val diceSetIdLong = diceSetId?.toLongOrNull() // Convert ID to Long for ViewModel
    // ViewModel instance scoped to this screen, created by its factory.
    val viewModel: CreateEditDiceSetViewModel = viewModel(
        factory = CreateEditDiceSetViewModelFactory(application, diceSetIdLong)
    )

    // Collecting state from the ViewModel.
    val currentSetName by viewModel.setName.collectAsState()
    val diceConfigs by viewModel.diceConfigs.collectAsState()
    val saveErrorId by viewModel.saveError.collectAsState() // For displaying field-specific errors.

    // Screen title determined by whether a diceSetId is provided (edit mode vs. create mode).
    val screenTitle = if (diceSetIdLong == null) {
        stringResource(id = R.string.create_dice_set_screen_title)
    } else {
        stringResource(id = R.string.edit_dice_set_screen_title)
    }
    // Default name for a new dice set, used for auto-selection logic.
    val defaultNewSetName = stringResource(id = R.string.default_new_dice_set_name)

    // State for the dice set name TextField.
    var setNameFieldValue by remember(currentSetName, defaultNewSetName) {
        mutableStateOf(TextFieldValue(currentSetName, TextRange(currentSetName.length)))
    }
    // Flag to control automatic text selection in the name field on first focus for new sets.
    var selectAllOnNextFocus by remember { mutableStateOf(diceSetIdLong == null && currentSetName == defaultNewSetName) }

    // UI state for the "Add/Edit Dice Configuration" section.
    var showAddDiceSection by remember { mutableStateOf(false) } // Toggles visibility of the input section.
    var selectedDiceType by remember { mutableStateOf(DiceType.D6) } // Currently selected dice type in the input section.
    var selectedDiceCountString by remember { mutableStateOf("1") } // Current dice count as a string in the input section.
    var dropdownExpanded by remember { mutableStateOf(false) } // Controls the visibility of the dice type dropdown.
    var editingConfigIndex by remember { mutableStateOf<Int?>(null) } // Index of the dice config being edited, or null if adding new.

    val snackbarHostState = remember { SnackbarHostState() }

    // Effect to update the name TextField value when the ViewModel's setName changes,
    // and to re-enable selectAllOnNextFocus if it's a new set with the default name.
    LaunchedEffect(currentSetName, defaultNewSetName) {
        if (currentSetName != setNameFieldValue.text) {
            setNameFieldValue = TextFieldValue(currentSetName, TextRange(currentSetName.length))
            if (diceSetIdLong == null && currentSetName == defaultNewSetName) {
                selectAllOnNextFocus = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // For displaying global messages like save success/failure (though not fully used for errors in provided code).
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
                    // Save button: triggers the save operation in the ViewModel.
                    IconButton(onClick = {
                        viewModel.saveDiceSet(onSuccess = { onNavigateBack() }) // Assumes ViewModel handles error display via saveErrorId or Toast for other errors.
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
                .padding(16.dp), // Overall padding for the screen content.
        ) {
            // Text field for the dice set name.
            OutlinedTextField(
                value = setNameFieldValue,
                onValueChange = {
                    setNameFieldValue = it
                    viewModel.updateSetName(it.text) // Update ViewModel as user types.
                    // If user changes the default name, disable auto-selection for subsequent focuses.
                    if (selectAllOnNextFocus && it.text != defaultNewSetName) {
                        selectAllOnNextFocus = false
                    }
                },
                label = { Text(stringResource(R.string.dice_set_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        // If it's a new set, the name is default, and this is the first focus, select all text.
                        if (focusState.isFocused && selectAllOnNextFocus && setNameFieldValue.text == defaultNewSetName) {
                            setNameFieldValue = setNameFieldValue.copy(
                                selection = TextRange(0, setNameFieldValue.text.length)
                            )
                            selectAllOnNextFocus = false // Prevent re-selection on subsequent focuses.
                        }
                    },
                singleLine = true,
                isError = saveErrorId == R.string.error_set_name_empty // Display error state if name is empty on save attempt.
            )
            // Display error message below the text field if applicable.
            if (saveErrorId == R.string.error_set_name_empty) {
                Text(
                    text = stringResource(id = R.string.error_set_name_empty),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp) // Indent error message.
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header for the dice configurations list and Add/Cancel button.
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
                    // If closing the section or if it was in edit mode, reset input fields and editing state.
                    if (!showAddDiceSection || wasEditing) {
                        editingConfigIndex = null
                        selectedDiceType = DiceType.D6
                        selectedDiceCountString = "1"
                        dropdownExpanded = false
                    }
                    // If opening to add a new config (not from clicking an existing one to edit), ensure fields are reset.
                    if (showAddDiceSection && !wasEditing && editingConfigIndex == null) {
                        selectedDiceType = DiceType.D6
                        selectedDiceCountString = "1"
                        dropdownExpanded = false
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_dice_config_desc)) // Icon remains 'Add'
                    Spacer(modifier = Modifier.width(4.dp))
                    Text( // Button text changes based on whether the input section is visible.
                        if (showAddDiceSection) stringResource(R.string.cancel)
                        else stringResource(R.string.add_dice_button)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Conditionally display the section for adding or editing a dice configuration.
            if (showAddDiceSection) {
                AddDiceConfigInputSection(
                    selectedDiceType = selectedDiceType,
                    onDiceTypeChange = { newType -> selectedDiceType = newType },
                    isDropdownExpanded = dropdownExpanded,
                    onDropdownExpandedChange = { expanded -> dropdownExpanded = expanded },
                    selectedDiceCountString = selectedDiceCountString,
                    onDiceCountChange = { newValue ->
                        // Validate and update dice count string.
                        if (newValue.isEmpty()) {
                            selectedDiceCountString = ""
                        } else if (newValue.all { char -> char.isDigit() }) {
                            val count = newValue.toIntOrNull()
                            if (count != null) {
                                // Clamp count to MAX_DICE_COUNT.
                                selectedDiceCountString = if (count > MAX_DICE_COUNT) MAX_DICE_COUNT.toString() else newValue
                            } else {
                                // If conversion to Int fails (e.g., too large for Int), set to MAX_DICE_COUNT.
                                selectedDiceCountString = MAX_DICE_COUNT.toString()
                            }
                        }
                        // Non-digit input is ignored.
                    },
                    isEditing = editingConfigIndex != null, // Pass whether this is an edit operation.
                    onConfirmClick = {
                        val count = selectedDiceCountString.toIntOrNull()
                        if (count != null && count > 0 && count <= MAX_DICE_COUNT) {
                            if (editingConfigIndex != null) { // Update existing config.
                                viewModel.updateDiceConfig(editingConfigIndex!!, selectedDiceType, count)
                            } else { // Add new config.
                                viewModel.addDiceConfig(DiceConfig(diceType = selectedDiceType, count = count))
                            }
                            // Reset input fields and state after confirmation.
                            selectedDiceType = DiceType.D6
                            selectedDiceCountString = "1"
                            showAddDiceSection = false
                            editingConfigIndex = null
                            dropdownExpanded = false
                        } else if (count != null && count > MAX_DICE_COUNT) { // Show error if count exceeds max.
                            Toast.makeText(context, context.getString(R.string.error_max_dice_count_exceeded, MAX_DICE_COUNT), Toast.LENGTH_SHORT).show()
                        } else { // Show error for other invalid counts (e.g., zero, empty, non-numeric).
                            Toast.makeText(context, context.getString(R.string.error_invalid_dice_count), Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display a message if no dice configurations have been added yet.
            if (diceConfigs.isEmpty()) {
                Text(
                    stringResource(R.string.no_dice_configs_yet) +
                            // Append specific error suffix if save failed due to no configs.
                            if (saveErrorId == R.string.error_no_dice_configs) " " + stringResource(R.string.error_no_dice_configs_suffix) else "",
                    color = if (saveErrorId == R.string.error_no_dice_configs) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            } else {
                // Display the list of currently added dice configurations.
                LazyColumn(modifier = Modifier.weight(1f)) { // `weight(1f)` makes the list scrollable and take available space.
                    itemsIndexed(diceConfigs, key = { _, item -> item.id }) { index, config ->
                        DiceConfigRow(
                            diceConfig = config,
                            isCurrentlyEditing = editingConfigIndex == index, // Highlight if this row is being edited.
                            onClick = {
                                // When a row is clicked, populate the input section with its details for editing.
                                editingConfigIndex = index
                                selectedDiceType = config.diceType
                                selectedDiceCountString = config.count.toString()
                                showAddDiceSection = true // Show the input section.
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


/**
 * A private composable section for inputting a single dice configuration.
 * It includes a dropdown for dice type and a text field for the count.
 *
 * @param selectedDiceType The currently selected [DiceType] for the input.
 * @param onDiceTypeChange Callback invoked when the user selects a new dice type.
 * @param isDropdownExpanded State indicating whether the dice type dropdown is currently expanded.
 * @param onDropdownExpandedChange Callback to change the expanded state of the dropdown.
 * @param selectedDiceCountString The current dice count as a string.
 * @param onDiceCountChange Callback invoked when the dice count string changes.
 * @param isEditing True if this section is for editing an existing configuration, false for adding a new one.
 * @param onConfirmClick Callback invoked when the user confirms the addition or update.
 * @param modifier Optional [Modifier] for this composable.
 */
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
        // Box acting as a clickable area for the dice type dropdown.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { // Toggles the dropdown menu visibility.
                    onDropdownExpandedChange(!isDropdownExpanded)
                }
        ) {
            // Visually, this TextField shows the selected dice type. It's read-only.
            // It's "disabled" to prevent direct text input but styled to look interactive.
            OutlinedTextField(
                value = selectedDiceType.displayName, // Display name of the selected DiceType.
                onValueChange = {}, // No-op as it's read-only here.
                label = { Text(stringResource(R.string.dice_type_label)) },
                readOnly = true,
                enabled = false, // Makes the TextField non-interactive directly.
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select dice type") }, // Dropdown indicator.
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                colors = TextFieldDefaults.colors( // Custom colors to make the disabled field look enabled.
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = Color.Transparent,
                    disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // The actual dropdown menu.
            DropdownMenu(
                expanded = isDropdownExpanded,
                onDismissRequest = { onDropdownExpandedChange(false) }, // Close if clicked outside.
                modifier = Modifier
                    .fillMaxWidth()
                    .background(fr.antoinehory.divination.ui.theme.OrakniumBackground) // Custom theme background.
                    .border(BorderStroke(1.dp, fr.antoinehory.divination.ui.theme.OrakniumGold)) // Custom theme border.
            ) {
                // Iterate over all DiceType enum values to create menu items.
                DiceType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                type.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        onClick = {
                            onDiceTypeChange(type) // Update selected type.
                            onDropdownExpandedChange(false) // Close dropdown.
                        },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp) // Specific padding from original code.
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // TextField for inputting the quantity of the selected dice type.
        OutlinedTextField(
            value = selectedDiceCountString,
            onValueChange = onDiceCountChange, // Handles validation and updates.
            label = { Text(stringResource(R.string.quantity_label)) },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), // Numeric keyboard.
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Button to confirm adding or updating the dice configuration.
        Button(
            onClick = onConfirmClick,
            modifier = Modifier.align(Alignment.End) // Align to the end of the row/column.
        ) {
            Text(
                stringResource( // Button text changes based on whether it's an edit or add operation.
                    if (isEditing) R.string.update_button
                    else R.string.confirm_add_to_set_button
                )
            )
        }
    }
}

/**
 * Displays a single row in the list of dice configurations.
 * Shows the dice type, count, and provides controls for incrementing,
 * decrementing, or removing the configuration.
 *
 * @param diceConfig The [DiceConfig] data for this row.
 * @param isCurrentlyEditing True if this row represents the configuration currently being edited in the input section.
 * @param onClick Callback invoked when the row is clicked, typically to initiate editing.
 * @param onIncrement Callback to increment the count of this dice configuration.
 * @param onDecrement Callback to decrement the count of this dice configuration.
 * @param onRemove Callback to remove this dice configuration from the set.
 * @param modifier Optional [Modifier] for this composable.
 */
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
    // Apply a slight background tint if this row is currently being edited.
    val backgroundColor = if (isCurrentlyEditing) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick) // Row click triggers editing.
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes dice info and controls apart.
    ) {
        // Display dice type (e.g., "D6").
        Text(
            text = diceConfig.diceType.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // Takes up available space, pushing controls to the right.
        )

        // Row for count display and +/- buttons.
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, enabled = diceConfig.count > 1) { // Decrement enabled if count > 1.
                Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.decrement_dice_count_desc))
            }
            Text(
                text = diceConfig.count.toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 8.dp) // Space around the count number.
            )
            IconButton(onClick = onIncrement, enabled = diceConfig.count < MAX_DICE_COUNT) { // Increment enabled if count < MAX.
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.increment_dice_count_desc))
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove) { // Button to remove this dice configuration.
                Icon(Icons.Filled.Delete, stringResource(R.string.remove_dice_config_desc))
            }
        }
    }
}


/**
 * Preview composable for the [CreateEditDiceSetScreen] in "Create New Set" mode.
 * This preview uses a local ViewModel setup with a null diceSetId to simulate
 * the creation flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Create New Dice Set Preview")
@Composable
fun CreateEditDiceSetScreenNewSetPreview() {
    val application = LocalContext.current.applicationContext as Application
    // Instantiate ViewModel with null ID for "create new" mode in preview.
    val previewViewModel = CreateEditDiceSetViewModelFactory(application, null).create(CreateEditDiceSetViewModel::class.java)

    DivinationAppTheme {
        // State mirroring for the preview, as it would be in the actual screen.
        val snackbarHostState = remember { SnackbarHostState() }
        val screenTitle = "Create Dice Set (Preview)" // Static title for preview.
        val defaultNewSetNameInPreview = stringResource(id = R.string.default_new_dice_set_name)

        // Mimic ViewModel state collection for the preview.
        val currentSetNameFromViewModel by previewViewModel.setName.collectAsState()
        var setNameFieldValueInPreview by remember(currentSetNameFromViewModel, defaultNewSetNameInPreview) {
            mutableStateOf(TextFieldValue(currentSetNameFromViewModel, TextRange(currentSetNameFromViewModel.length)))
        }
        var selectAllOnNextFocusInPreview by remember { mutableStateOf(true) }


        val diceConfigsFromVM by previewViewModel.diceConfigs.collectAsState()
        // Determine initial state for editing/adding section in preview.
        var editingConfigIndexInPreview by remember { mutableStateOf<Int?>(if (diceConfigsFromVM.isNotEmpty()) 0 else null) }
        var showAddSectionInPreview by remember { mutableStateOf(editingConfigIndexInPreview != null) }

        // State for the AddDiceConfigInputSection part of the preview.
        var selectedDiceTypeInPreview by remember { mutableStateOf(if (editingConfigIndexInPreview != null && diceConfigsFromVM.isNotEmpty()) diceConfigsFromVM[0].diceType else DiceType.D6) }
        var selectedDiceCountInPreview by remember { mutableStateOf(if (editingConfigIndexInPreview != null && diceConfigsFromVM.isNotEmpty()) diceConfigsFromVM[0].count.toString() else "1") }
        var dropdownExpandedInPreview by remember { mutableStateOf(false) }


        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar( // Simplified TopAppBar for preview.
                    title = { Text(screenTitle) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { /* No-op for preview */ }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* No-op for preview */ }) {
                            Icon(Icons.Filled.Done, "Save")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column( // Main content column for the preview.
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
            ) {
                // Dice Set Name TextField (copied from actual screen for consistency).
                OutlinedTextField(
                    value = setNameFieldValueInPreview,
                    onValueChange = {
                        setNameFieldValueInPreview = it
                        if (selectAllOnNextFocusInPreview && it.text != defaultNewSetNameInPreview) {
                            selectAllOnNextFocusInPreview = false
                        }
                    },
                    label = { Text("Set Name") }, // Simplified label for preview clarity.
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

                // Dice Configurations Section Header (copied from actual screen).
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dice Configurations", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { // Simplified logic for preview.
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

                // Add/Edit Dice Config Input Section (copied from actual screen).
                if (showAddSectionInPreview) {
                    AddDiceConfigInputSection(
                        selectedDiceType = selectedDiceTypeInPreview,
                        onDiceTypeChange = { selectedDiceTypeInPreview = it},
                        isDropdownExpanded = dropdownExpandedInPreview,
                        onDropdownExpandedChange = { expanded ->
                            dropdownExpandedInPreview = expanded
                        },
                        selectedDiceCountString = selectedDiceCountInPreview,
                        onDiceCountChange = { selectedDiceCountInPreview = it },
                        isEditing = editingConfigIndexInPreview != null,
                        onConfirmClick = { // Simplified ViewModel interaction for preview.
                            val count = selectedDiceCountInPreview.toIntOrNull()
                            if (count != null && count > 0 && count <= MAX_DICE_COUNT) {
                                if (editingConfigIndexInPreview != null) {
                                    previewViewModel.updateDiceConfig(editingConfigIndexInPreview!!,selectedDiceTypeInPreview, count)
                                } else {
                                    previewViewModel.addDiceConfig(DiceConfig(diceType = selectedDiceTypeInPreview, count = count))
                                }
                                showAddSectionInPreview = false
                                editingConfigIndexInPreview = null
                                dropdownExpandedInPreview = false
                            }
                            // Note: Toast messages for errors are omitted in preview for simplicity.
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                // List of Dice Configs (copied from actual screen).
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(diceConfigsFromVM, key = { _, item -> item.id }) { index, config -> // Use index as key if item.id is not unique or stable during preview changes.
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
