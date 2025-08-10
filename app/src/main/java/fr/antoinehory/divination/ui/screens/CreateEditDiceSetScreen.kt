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
import androidx.compose.foundation.layout.PaddingValues // Ensured import for DropdownMenuItem
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
import fr.antoinehory.divination.viewmodels.UiEvent
// import fr.antoinehory.divination.viewmodels.MAX_TOTAL_DICE_IN_SET // Only needed for preview if displaying that specific error text
import kotlinx.coroutines.flow.collectLatest

/**
 * Maximum allowed count for a single type of dice in a configuration.
 * This constant is used by UI elements like [DiceConfigRow] for enabling/disabling controls.
 * The ViewModel uses its own MAX_DICE_COUNT_VM for internal clamping.
 */
const val MAX_DICE_COUNT = 1000

/**
 * Composable screen for creating a new dice set or editing an existing one.
 * Users can define a name for the set and add multiple dice configurations,
 * specifying the type of dice (e.g., D6, D20) and the count for each type.
 * This screen interacts with [CreateEditDiceSetViewModel] to manage state and business logic.
 *
 * @param onNavigateBack Callback invoked when the user navigates back from this screen
 *                       (e.g., by pressing the back button or after a successful save).
 * @param diceSetId The ID of the dice set to be edited. If `null`, the screen operates
 *                  in "create new set" mode.
 */
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

    val selectedDiceTypeForInput by viewModel.currentDiceTypeForInput.collectAsState()
    val selectedDiceCountStringForInput by viewModel.currentDiceCountStringForInput.collectAsState()
    val editingConfigIndexVm by viewModel.editingConfigIndex.collectAsState()

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
    var dropdownExpanded by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = Unit) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    val message = if (event.args.isEmpty()) {
                        context.getString(event.messageResId)
                    } else {
                        context.getString(event.messageResId, *event.args.toTypedArray())
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    LaunchedEffect(key1 = Unit) {
        viewModel.navigateBackEvent.collectLatest {
            onNavigateBack()
        }
    }

    LaunchedEffect(currentSetName, defaultNewSetName) {
        if (currentSetName != setNameFieldValue.text) {
            setNameFieldValue = TextFieldValue(currentSetName, TextRange(currentSetName.length))
            if (diceSetIdLong == null && currentSetName == defaultNewSetName) {
                selectAllOnNextFocus = true
            }
        }
    }

    LaunchedEffect(editingConfigIndexVm) {
        if (editingConfigIndexVm != null && !showAddDiceSection) {
            showAddDiceSection = true
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
                    IconButton(onClick = { viewModel.saveDiceSet() }) {
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
            // Note: Error for R.string.error_max_total_dice_exceeded (if set in saveErrorId)
            // will be shown via Toast from UiEvent, as per user's original design
            // that did not have a dedicated Text field for this specific error.

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
                    showAddDiceSection = !showAddDiceSection
                    if (showAddDiceSection) {
                        if (editingConfigIndexVm == null) {
                            viewModel.prepareForNewConfig()
                        }
                    } else {
                        viewModel.cancelConfigInput()
                        dropdownExpanded = false
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_dice_config_desc))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (showAddDiceSection) stringResource(R.string.cancel)
                        else stringResource(R.string.add_dice_button)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (showAddDiceSection) {
                AddDiceConfigInputSection(
                    selectedDiceType = selectedDiceTypeForInput,
                    onDiceTypeChange = { viewModel.updateDiceTypeForInput(it) },
                    isDropdownExpanded = dropdownExpanded,
                    onDropdownExpandedChange = { expanded -> dropdownExpanded = expanded },
                    selectedDiceCountString = selectedDiceCountStringForInput,
                    onDiceCountChange = { viewModel.updateDiceCountStringForInput(it) },
                    isEditing = editingConfigIndexVm != null,
                    onConfirmClick = {
                        if (viewModel.submitDiceConfig()) {
                            showAddDiceSection = false
                            dropdownExpanded = false
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (diceConfigs.isEmpty()) {
                Text(
                    stringResource(R.string.no_dice_configs_yet) +
                            (if (saveErrorId == R.string.error_no_dice_configs) " " + stringResource(R.string.error_no_dice_configs_suffix) else ""),
                    color = if (saveErrorId == R.string.error_no_dice_configs) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    itemsIndexed(diceConfigs, key = { _, item -> item.id }) { index, config ->
                        DiceConfigRow(
                            diceConfig = config,
                            isCurrentlyEditing = editingConfigIndexVm == index,
                            onClick = {
                                viewModel.prepareToEditConfig(index)
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
                .clickable { onDropdownExpandedChange(!isDropdownExpanded) }
        ) {
            OutlinedTextField(
                value = selectedDiceType.displayName,
                onValueChange = {},
                label = { Text(stringResource(R.string.dice_type_label)) },
                readOnly = true,
                enabled = false,
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
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline))
            ) {
                DiceType.values().forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                type.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface // As per user's version
                            )
                        },
                        onClick = {
                            onDiceTypeChange(type)
                            onDropdownExpandedChange(false)
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
            IconButton(onClick = onIncrement, enabled = diceConfig.count < MAX_DICE_COUNT) { // Uses screen's MAX_DICE_COUNT
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
    val previewViewModel = CreateEditDiceSetViewModelFactory(application, null)
        .create(CreateEditDiceSetViewModel::class.java)

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
        val saveErrorIdFromVM by previewViewModel.saveError.collectAsState()

        val selectedDiceTypeInPreview by previewViewModel.currentDiceTypeForInput.collectAsState()
        val selectedDiceCountInPreview by previewViewModel.currentDiceCountStringForInput.collectAsState()
        val editingConfigIndexInPreview by previewViewModel.editingConfigIndex.collectAsState()

        var showAddSectionInPreview by remember { mutableStateOf(false) }
        var dropdownExpandedInPreview by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            previewViewModel.uiEvents.collectLatest { event ->
                if (event is UiEvent.ShowToast) {
                    val message = if (event.args.isEmpty()) {
                        application.getString(event.messageResId)
                    } else {
                        application.getString(event.messageResId, *event.args.toTypedArray())
                    }
                    println("Preview Toast: $message")
                }
            }
        }
        LaunchedEffect(Unit) {
            previewViewModel.navigateBackEvent.collectLatest {
                println("Preview: Navigate Back triggered")
            }
        }
        LaunchedEffect(editingConfigIndexInPreview) {
            if (editingConfigIndexInPreview != null && !showAddSectionInPreview) {
                showAddSectionInPreview = true
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    colors = TopAppBarDefaults.topAppBarColors( /* ... */ ),
                    navigationIcon = {
                        IconButton(onClick = { println("Preview: Navigate Back Clicked") }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Navigate back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { previewViewModel.saveDiceSet() }) {
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
                        previewViewModel.updateSetName(it.text)
                        if (selectAllOnNextFocusInPreview && it.text != defaultNewSetNameInPreview) {
                            selectAllOnNextFocusInPreview = false
                        }
                    },
                    label = { Text("Set Name") },
                    modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                        if (focusState.isFocused && selectAllOnNextFocusInPreview && setNameFieldValueInPreview.text == defaultNewSetNameInPreview) {
                            setNameFieldValueInPreview = setNameFieldValueInPreview.copy(
                                selection = TextRange(0, setNameFieldValueInPreview.text.length)
                            )
                            selectAllOnNextFocusInPreview = false
                        }
                    },
                    singleLine = true,
                    isError = saveErrorIdFromVM == R.string.error_set_name_empty
                )
                if (saveErrorIdFromVM == R.string.error_set_name_empty) {
                    Text(
                        text = stringResource(id = R.string.error_set_name_empty),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
                // Preview doesn't show text for R.string.error_max_total_dice_exceeded, relies on Toast like main code
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), /* ... */ horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dice Configurations", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = {
                        showAddSectionInPreview = !showAddSectionInPreview
                        if (showAddSectionInPreview) {
                            if (editingConfigIndexInPreview == null) {
                                previewViewModel.prepareForNewConfig()
                            }
                        } else {
                            previewViewModel.cancelConfigInput()
                            dropdownExpandedInPreview = false
                        }
                    }) {
                        Icon(Icons.Filled.Add, "Add/Cancel"); Spacer(modifier = Modifier.width(4.dp)); Text(if (showAddSectionInPreview) "Cancel" else "Add Die")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                if (showAddSectionInPreview) {
                    AddDiceConfigInputSection(
                        selectedDiceType = selectedDiceTypeInPreview,
                        onDiceTypeChange = { previewViewModel.updateDiceTypeForInput(it) },
                        isDropdownExpanded = dropdownExpandedInPreview,
                        onDropdownExpandedChange = { dropdownExpandedInPreview = it },
                        selectedDiceCountString = selectedDiceCountInPreview,
                        onDiceCountChange = { previewViewModel.updateDiceCountStringForInput(it) },
                        isEditing = editingConfigIndexInPreview != null,
                        onConfirmClick = {
                            if (previewViewModel.submitDiceConfig()) {
                                showAddSectionInPreview = false
                                dropdownExpandedInPreview = false
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (diceConfigsFromVM.isEmpty()) {
                    Text(
                        stringResource(R.string.no_dice_configs_yet) +
                                (if (saveErrorIdFromVM == R.string.error_no_dice_configs) " " + stringResource(R.string.error_no_dice_configs_suffix) else ""),
                        color = if (saveErrorIdFromVM == R.string.error_no_dice_configs) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(diceConfigsFromVM, key = { index, item -> item.id + index.toString() }) { index, config ->
                            DiceConfigRow(
                                diceConfig = config,
                                isCurrentlyEditing = editingConfigIndexInPreview == index,
                                onClick = {
                                    previewViewModel.prepareToEditConfig(index)
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
}

