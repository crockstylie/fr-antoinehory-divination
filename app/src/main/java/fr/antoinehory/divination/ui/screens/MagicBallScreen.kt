package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
// import androidx.compose.foundation.Image // SUPPRIMÉ: L'image n'est plus utilisée
import androidx.compose.foundation.clickable // ASSUREZ-VOUS QUE C'EST BIEN LÀ
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.painterResource // SUPPRIMÉ: L'image n'est plus utilisée
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.MagicBallViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.MagicBallViewModelFactory

@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val magicBallViewModel: MagicBallViewModel = viewModel(
        factory = MagicBallViewModelFactory(application, launchLogRepository)
    )

    val responseText by magicBallViewModel.currentResponse.collectAsState()
    val isPredicting by magicBallViewModel.isPredicting.collectAsState()

    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    LaunchedEffect(interactionViewModel, magicBallViewModel, isPredicting) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isPredicting) {
                magicBallViewModel.getNewPrediction()
            }
        }
    }

    val textAlpha by animateFloatAsState(
        targetValue = if (isPredicting) 0.6f else 1.0f,
        animationSpec = tween(durationMillis = 300),
        label = "textAlphaMagicBall"
    )

    val textColor by animateColorAsState(
        targetValue = if (isPredicting) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.onBackground,
        animationSpec = tween(durationMillis = 300),
        label = "textColorMagicBall"
    )

    AppScaffold(
        title = stringResource(id = R.string.magic_ball_screen_title),
        canNavigateBack = true,
        onNavigateBack = onNavigateBack
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Le padding de AppScaffold est appliqué ici
                .clickable { // MODIFIÉ: Le Column entier devient cliquable
                    if (!isPredicting) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                }
                .padding(horizontal = 32.dp, vertical = 16.dp), // Padding interne supplémentaire
            verticalArrangement = Arrangement.Center, // MODIFIÉ: Centrer le texte verticalement
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .alpha(textAlpha)
                // .weight(1f) // SUPPRIMÉ: Plus nécessaire avec Arrangement.Center
                // .wrapContentHeight(Alignment.CenterVertically) // SUPPRIMÉ: Plus nécessaire
            )

            // L'Image a été supprimée d'ici

            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            // Affichage du message d'erreur si pas d'interaction possible par secousse
            // et que le message est le message initial.
            // On le place en bas pour ne pas trop gêner.
            // On pourrait utiliser un Spacer pour le pousser vers le bas ou le mettre dans un Box séparé
            // si l'on souhaite un positionnement plus précis.
            // Pour l'instant, on le laisse dans le flux du Column.
            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                Spacer(modifier = Modifier.weight(1f)) // Pousse le message d'erreur vers le bas
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp) // Un peu de padding en bas
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        Text("Preview for MagicBallScreen needs adjustment for ViewModel with repository.")
    }
}