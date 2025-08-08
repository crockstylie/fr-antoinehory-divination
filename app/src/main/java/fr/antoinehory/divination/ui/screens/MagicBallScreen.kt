package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
// import androidx.compose.material.icons.Icons // Potentially unused now
// import androidx.compose.material.icons.filled.PieChart // Unused now
// import androidx.compose.material3.BottomAppBar // Unused now
// import androidx.compose.material3.Icon // Potentially unused if only for old bottom bar
// import androidx.compose.material3.IconButton // Potentially unused if only for old bottom bar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar // AJOUT: Import de la barre de navigation commune
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
// import fr.antoinehory.divination.ui.theme.OrakniumGold // Potentially unused if only for old bottom bar
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.MagicBallViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.MagicBallViewModelFactory


@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit,
    onNavigateToInfo: () -> Unit // AJOUT: Paramètre pour la navigation vers l'écran d'info
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication

    val magicBallViewModel: MagicBallViewModel = viewModel(
        factory = MagicBallViewModelFactory(application, application.launchLogRepository)
    )

    val responseText by magicBallViewModel.currentResponse.collectAsState()
    val isPredicting by magicBallViewModel.isPredicting.collectAsState()
    val recentLogs by magicBallViewModel.recentLogs.collectAsState()

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
        onNavigateBack = onNavigateBack,
        actions = {
            // L'icône PieChart est maintenant gérée par BottomAppNavigationBar
        },
        bottomBar = {
            // MODIFIÉ: Utilisation de la barre de navigation commune
            BottomAppNavigationBar(
                onSettingsClick = { /* Action pour Settings, masquée pour l'instant */ },
                onStatsClick = { onNavigateToStats(GameType.MAGIC_EIGHT_BALL) },
                onInfoClick = onNavigateToInfo,
                showSettingsButton = false // Cache le bouton Settings pour cet écran
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .clickable {
                    if (!isPredicting) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                }
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = responseText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = textColor,
                modifier = Modifier
                    .alpha(textAlpha)
            )

            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.MAGIC_EIGHT_BALL
            )

            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "MagicBallScreen - Idle")
@Composable
fun MagicBallScreenPreviewIdle() {
    DivinationAppTheme {
        MagicBallScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {} // AJOUT pour la preview
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "MagicBallScreen Landscape")
@Composable
fun MagicBallScreenLandscapePreview() {
    DivinationAppTheme {
        MagicBallScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {} // AJOUT pour la preview
        )
    }
}

