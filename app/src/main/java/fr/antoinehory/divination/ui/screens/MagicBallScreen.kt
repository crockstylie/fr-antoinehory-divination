package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext // AJOUT: Pour LocalContext
import androidx.compose.ui.res.painterResource
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
// AJOUTS :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.MagicBallViewModelFactory

@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    // magicBallViewModel: MagicBallViewModel = viewModel(), // Retiré pour initialisation avec factory
    interactionViewModel: InteractionDetectViewModel = viewModel()
) {
    // AJOUT: Récupération du LaunchLogRepository et Application
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    // AJOUT: Initialisation du MagicBallViewModel avec la factory
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
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .alpha(textAlpha)
                    .weight(1f)
                    .wrapContentHeight(Alignment.CenterVertically)
            )

            Image(
                painter = painterResource(id = R.drawable.magic_ball_default),
                contentDescription = stringResource(R.string.magic_ball_content_description),
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp)
                    .clickable {
                        if (!isPredicting) {
                            if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                                interactionViewModel.userTappedScreen()
                            }
                        }
                    }
            )

            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        // MagicBallScreen(onNavigateBack = {}) // Commenté pour l'instant
        Text("Preview for MagicBallScreen needs adjustment for ViewModel with repository.")
    }
}

