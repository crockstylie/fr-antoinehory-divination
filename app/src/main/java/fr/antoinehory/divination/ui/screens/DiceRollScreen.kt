package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.DiceRollViewModel

@Composable
fun DiceRollScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiceRollViewModel = viewModel()
) {
    val currentMessage by viewModel.currentMessage.collectAsState()
    val diceValue by viewModel.diceValue.collectAsState()
    val isProcessingShake by viewModel.isProcessingShake.collectAsState()
    val isAccelerometerAvailable by viewModel.isAccelerometerAvailable.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.registerSensorListener()
                // viewModel.resetGame() // Décommente si tu veux réinitialiser à chaque fois que l'écran revient
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.unregisterSensorListener()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.unregisterSensorListener()
        }
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake || diceValue == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isProcessingShake) 0 else 100),
        label = "diceImageAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake && diceValue == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "diceTextAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.dice_roll_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val initialNoAccelerometerText = stringResource(id = R.string.dice_initial_prompt_no_accelerometer)
            if (!isAccelerometerAvailable && currentMessage == initialNoAccelerometerText) {
                Text(
                    text = stringResource(id = R.string.dice_accelerometer_not_available_ui_message),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                // Tu pourrais ajouter un bouton ici pour lancer le dé manuellement
                // Button(onClick = { /* TODO: viewModel.manualRoll() */ }) { Text("Lancer le dé") }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(120.dp), // Ajuste la taille si besoin
                contentAlignment = Alignment.Center
            ) {
                if (diceValue != null && !isProcessingShake) {
                    val painterId = when (diceValue) {
                        1 -> R.drawable.ic_dice_1
                        2 -> R.drawable.ic_dice_2
                        3 -> R.drawable.ic_dice_3
                        4 -> R.drawable.ic_dice_4
                        5 -> R.drawable.ic_dice_5
                        6 -> R.drawable.ic_dice_6
                        else -> null // Ne devrait pas arriver si diceValue est entre 1 et 6
                    }
                    val contentDescId = when (diceValue) {
                        1 -> R.string.dice_icon_description_1
                        2 -> R.string.dice_icon_description_2
                        3 -> R.string.dice_icon_description_3
                        4 -> R.string.dice_icon_description_4
                        5 -> R.string.dice_icon_description_5
                        6 -> R.string.dice_icon_description_6
                        else -> R.string.dice_icon_description_empty
                    }

                    painterId?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = stringResource(id = contentDescId),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(imageAlpha)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiceRollScreenPreview() {
    DivinationAppTheme {
        // Si tu as besoin d'un Application context pour le Preview du ViewModel :
        // val context = LocalContext.current
        // val previewViewModel = DiceRollViewModel(context.applicationContext as Application)
        // DiceRollScreen(onNavigateBack = {}, viewModel = previewViewModel)

        // Version simple :
        DiceRollScreen(onNavigateBack = {})
    }
}
