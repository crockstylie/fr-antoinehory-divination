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
import fr.antoinehory.divination.data.InteractionMode // <-- AJOUTÉ : Pour vérifier activeInteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.CoinFace
import fr.antoinehory.divination.viewmodels.CoinFlipViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel

@Composable
fun CoinFlipScreen(
    onNavigateBack: () -> Unit,
    coinFlipViewModel: CoinFlipViewModel = viewModel(),
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    val currentMessage by coinFlipViewModel.currentMessage.collectAsState()
    val coinFace by coinFlipViewModel.coinFace.collectAsState()
    val isFlipping by coinFlipViewModel.isFlipping.collectAsState()

    // Collecter les préférences d'interaction et la disponibilité du matériel
    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()
    // isTapAvailable n'est plus un StateFlow séparé dans InteractionDetectViewModel,
    // car le tap est toujours "disponible" et son activation dépend de activeInteractionMode.
    // Les champs liés au microphone sont supprimés.

    // Observer les déclencheurs d'interaction
    LaunchedEffect(interactionViewModel, coinFlipViewModel, isFlipping) { // Ajouter isFlipping aux clés
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isFlipping) {
                coinFlipViewModel.performCoinFlip()
            }
        }
    }

    val context = LocalContext.current
    val headsBitmap = remember(context) {
        ContextCompat.getDrawable(context, R.drawable.ic_heads)?.toBitmap()?.asImageBitmap()
    }
    val tailsBitmap = remember(context) {
        ContextCompat.getDrawable(context, R.drawable.ic_tails)?.toBitmap()?.asImageBitmap()
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
                        // Si le mode TAP est actif, l'action de clic direct sur l'écran
                        // devrait aussi déclencher le flip via le système d'interaction.
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen() // Informe le système de détection de tap
                            // Le coinFlipViewModel.performCoinFlip() sera appelé par le LaunchedEffect
                            // lorsqu'un TapEvent sera émis par interactionTriggered.
                            // Si vous voulez une réactivité immédiate en plus de l'événement,
                            // vous pouvez appeler coinFlipViewModel.performCoinFlip() ici aussi, mais cela pourrait
                            // potentiellement entraîner un double flip si l'événement arrive très vite.
                            // Il est plus propre de laisser l'orchestrateur gérer le déclenchement.
                        } else {
                            // Si TAP n'est pas le mode actif, le clic ne fait rien d'automatique
                            // via le système d'interaction. Vous pourriez choisir de quand même lancer la pièce
                            // comme fallback, ou ne rien faire.
                            // Pour l'instant, laissons le système d'interaction principal gérer.
                            // Si vous voulez que le clic fonctionne toujours comme un "tap" de dernier recours :
                            // coinFlipViewModel.performCoinFlip()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Message si aucune interaction n'est possible (si SHAKE est le mode actif mais non disponible)
            val initialGenericMessage = stringResource(id = R.string.coin_flip_initial_prompt_generic)

            // Le seul cas où "aucune interaction n'est possible" est si le mode SHAKE est sélectionné
            // ET que le matériel pour le shake n'est pas disponible.
            // Le mode TAP est toujours considéré comme disponible.
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && currentMessage == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.coin_flip_no_interaction_method_active),
                    // Vous devrez AJOUTER cette nouvelle chaîne de ressource, par exemple :
                    // <string name="coin_flip_shake_unavailable_prompt">Le mode "Secouer" est actif mais non disponible sur cet appareil. Changez de mode dans les paramètres ou essayez de taper sur l'écran.</string>
                    // Ou plus simplement :
                    // <string name="coin_flip_shake_unavailable_prompt">Mode "Secouer" actif mais non disponible. Vérifiez les paramètres.</string>
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
                else if (isFlipping) {
                    // Optionnel: CircularProgressIndicator()
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
    DivinationAppTheme {
        CoinFlipScreen(onNavigateBack = {})
    }
}
