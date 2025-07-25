package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource // << AJOUTER CET IMPORT
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R // << AJOUTER CET IMPORT (peut déjà y être)
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CoinFace
import fr.antoinehory.divination.viewmodels.CoinFlipViewModel

@Composable
fun CoinFlipScreen(
    onNavigateBack: () -> Unit,
    viewModel: CoinFlipViewModel = viewModel()
) {
    val currentMessage by viewModel.currentMessage.collectAsState()
    val coinFace by viewModel.coinFace.collectAsState()
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
        }
    }

    val context = LocalContext.current
    val headsBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_heads)?.toBitmap()?.asImageBitmap()
    }
    val tailsBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.ic_tails)?.toBitmap()?.asImageBitmap()
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake || coinFace == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isProcessingShake) 0 else 100),
        label = "coinImageAlpha" // Ajout du label pour les tests/inspections
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isProcessingShake && coinFace == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "coinTextAlpha" // Ajout du label
    )

    AppScaffold(
        title = stringResource(id = R.string.coin_flip_screen_title), // << MODIFIÉ
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
            // Le message d'erreur si l'accéléromètre n'est pas disponible et que l'invite est de secouer
            val initialNoAccelerometerText = stringResource(id = R.string.coin_flip_initial_prompt_no_accelerometer)
            if (!isAccelerometerAvailable && currentMessage == initialNoAccelerometerText) {
                Text(
                    text = stringResource(id = R.string.coin_flip_accelerometer_not_available_ui_message), // << MODIFIÉ
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.size(150.dp)) {
                if (coinFace != null && !isProcessingShake) {
                    val bitmapToShow = if (coinFace == CoinFace.HEADS) headsBitmap else tailsBitmap
                    bitmapToShow?.let {
                        Image(
                            bitmap = it,
                            contentDescription = if (coinFace == CoinFace.HEADS) {
                                stringResource(R.string.coin_flip_result_heads) // << MODIFIÉ (peut aussi être une string dédiée "Image de Pile")
                            } else {
                                stringResource(R.string.coin_flip_result_tails) // << MODIFIÉ (peut aussi être une string dédiée "Image de Face")
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(imageAlpha)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = currentMessage, // Provient du ViewModel, déjà internationalisé
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinFlipScreenPreview() {
    DivinationAppTheme {
        CoinFlipScreen(onNavigateBack = {})
    }
}
