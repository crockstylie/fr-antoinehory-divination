package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CoinFace
import fr.antoinehory.divination.viewmodels.CoinFlipViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
// Imports ajoutés :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.CoinFlipViewModelFactory

@Composable
fun CoinFlipScreen(
    onNavigateBack: () -> Unit,
    // La valeur par défaut pour coinFlipViewModel est retirée ici,
    // car nous l'initialisons dans le corps avec la factory.
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    // Récupération du LaunchLogRepository via la classe Application
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    // Initialisation du CoinFlipViewModel avec la factory
    val coinFlipViewModel: CoinFlipViewModel = viewModel(
        factory = CoinFlipViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by coinFlipViewModel.currentMessage.collectAsState()
    val coinFace by coinFlipViewModel.coinFace.collectAsState()
    val isFlipping by coinFlipViewModel.isFlipping.collectAsState()

    // Collecter les préférences d'interaction et la disponibilité du matériel
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    LaunchedEffect(interactionViewModel, coinFlipViewModel, isFlipping) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isFlipping) {
                coinFlipViewModel.performCoinFlip()
            }
        }
    }

    val currentLocalContext = LocalContext.current // Renommé pour éviter la confusion avec le context précédent
    val headsBitmap = remember(currentLocalContext) {
        ContextCompat.getDrawable(currentLocalContext, R.drawable.ic_heads)?.toBitmap()?.asImageBitmap()
    }
    val tailsBitmap = remember(currentLocalContext) {
        ContextCompat.getDrawable(currentLocalContext, R.drawable.ic_tails)?.toBitmap()?.asImageBitmap()
    }

    val imageAlpha by animateFloatAsState(
        targetValue = if (isFlipping || coinFace == null) 0f else 1f,
        animationSpec = tween(durationMillis = 300, delayMillis = if (isFlipping) 0 else 100),
        label = "coinImageAlpha"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (isFlipping && coinFace == null) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "coinTextAlpha"
    )

    AppScaffold(
        title = stringResource(id = R.string.coin_flip_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .clickable {
                    if (!isFlipping) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val initialGenericMessage = stringResource(id = R.string.coin_flip_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && currentMessage == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.coin_flip_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (coinFace != null && !isFlipping) {
                    val bitmapToShow = if (coinFace == CoinFace.HEADS) headsBitmap else tailsBitmap
                    val contentDesc = if (coinFace == CoinFace.HEADS) {
                        stringResource(R.string.coin_flip_result_heads_image_description)
                    } else {
                        stringResource(R.string.coin_flip_result_tails_image_description)
                    }

                    bitmapToShow?.let {
                        Image(
                            bitmap = it,
                            contentDescription = contentDesc,
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
fun CoinFlipScreenPreview() {
    // La preview ne fonctionnera plus directement comme ça car elle a besoin
    // d'un LaunchLogRepository. Pour la preview, vous pourriez passer un repository mocké
    // ou une instance basique si cela n'implique pas de contexte Android réel.
    // Pour l'instant, nous pouvons la laisser ainsi ou la commenter.
    DivinationAppTheme {
        // CoinFlipScreen(onNavigateBack = {}) // Commenté pour l'instant
        Text("Preview for CoinFlipScreen needs adjustment for ViewModel with repository.")
    }
}
