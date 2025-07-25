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
import androidx.compose.ui.graphics.ColorFilter
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
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.RPSOutcome
import fr.antoinehory.divination.viewmodels.RockPaperScissorsViewModel

@Composable
fun RockPaperScissorsScreen(
    onNavigateBack: () -> Unit,
    viewModel: RockPaperScissorsViewModel = viewModel()
) {
    val currentMessage by viewModel.currentMessage.collectAsState()
    val rpsOutcome by viewModel.rpsOutcome.collectAsState()
    val isProcessingShake by viewModel.isProcessingShake.collectAsState()
    val isAccelerometerAvailable by viewModel.isAccelerometerAvailable.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.registerSensorListener()
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
        targetValue = if (isProcessingShake || rpsOutcome == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isProcessingShake) 0 else 100),
        label = "rpsImageAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake && rpsOutcome == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "rpsTextAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.rps_screen_title),
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
            val initialNoAccelerometerText = stringResource(id = R.string.rps_initial_prompt_no_accelerometer)
            if (!isAccelerometerAvailable && currentMessage == initialNoAccelerometerText) {
                Text(
                    text = stringResource(id = R.string.rps_accelerometer_not_available_ui_message),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                val shouldShowImage = rpsOutcome != null && !isProcessingShake

                if (shouldShowImage) {
                    val painterId = when (rpsOutcome) {
                        RPSOutcome.ROCK -> R.drawable.ic_rps_rock
                        RPSOutcome.PAPER -> R.drawable.ic_rps_paper
                        RPSOutcome.SCISSORS -> R.drawable.ic_rps_scissors
                        null -> null // Ne devrait pas être atteint à cause de la condition rpsOutcome != null
                    }
                    val contentDescId = when (rpsOutcome) {
                        RPSOutcome.ROCK -> R.string.rps_icon_description_rock
                        RPSOutcome.PAPER -> R.string.rps_icon_description_paper
                        RPSOutcome.SCISSORS -> R.string.rps_icon_description_scissors
                        null -> R.string.general_content_description_empty
                    }

                    if (painterId != null) {
                        Image(
                            painter = painterResource(id = painterId),
                            contentDescription = stringResource(id = contentDescId),
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(imageAlpha), // imageAlpha est la valeur animée
                            colorFilter = ColorFilter.tint(OrakniumGold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha) // textAlpha est la valeur animée
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RockPaperScissorsScreenPreview() {
    DivinationAppTheme {
        // Version la plus simple, comme CoinFlipScreenPreview s'il fonctionne sans instance explicite
        RockPaperScissorsScreen(onNavigateBack = {})
    }
}

