package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
// import androidx.compose.ui.graphics.Color // Non utilisé directement ici
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// import androidx.compose.ui.unit.sp // Plus nécessaire ici si GameHistoryDisplay le gère
import androidx.lifecycle.viewmodel.compose.viewModel
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.InteractionMode
// AJOUT: Import du nouveau composant d'historique
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.viewmodels.MagicBallViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.MagicBallViewModelFactory


@Composable
fun MagicBallScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit
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
            // L'icône PieChart est maintenant dans la bottomBar
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = OrakniumGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateToStats(GameType.MAGIC_EIGHT_BALL) }) {
                        Icon(
                            imageVector = Icons.Filled.PieChart,
                            contentDescription = stringResource(id = R.string.game_stats_icon_description),
                            tint = OrakniumGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
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

            // MODIFICATION: Utilisation du composant GameHistoryDisplay
            // La logique `if (recentLogs.isNotEmpty()) { Spacer ... recentLogs.forEachIndexed ... }` est remplacée.
            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.MAGIC_EIGHT_BALL
                // Pour MagicBall, il est crucial que defaultLogResultFormatter dans GameHistoryDisplay
                // gère correctement les log.result. Si log.result est déjà la chaîne de réponse complète,
                // le formateur actuel (qui retourne logResult tel quel pour MAGIC_EIGHT_BALL) est correct.
                // Si log.result est un index ou une clé, le formateur devra être ajusté.
                // Basé sur le code original de MagicBallScreen, il semble que log.result soit
                // soit MagicBallViewModel.FALLBACK_LOG_IDENTIFIER, soit un index.
                // Le formateur actuel n'est PAS adapté à ce cas. Nous devons le revoir.
            )
            // FIN SECTION HISTORIQUE MODIFIÉE


            val initialGenericMessage = stringResource(id = R.string.magic_ball_initial_prompt_generic)
            val noShakeInteractionPossible = interactionPrefs.activeInteractionMode == InteractionMode.SHAKE && !isShakeAvailable

            // Ce bloc if/else gère le message d'erreur si aucune méthode d'interaction n'est active.
            // Il est placé après l'historique. Si l'historique est court ou vide,
            // et que ce message d'erreur s'affiche, le layout sera un peu différent
            // par rapport à avant où l'historique pouvait occuper plus d'espace vertical.
            // Ceci est à vérifier visuellement. Le Spacer(modifier = Modifier.weight(1f))
            // pourrait avoir besoin d'être ajusté ou déplacé en fonction du résultat souhaité.
            if (noShakeInteractionPossible && responseText == initialGenericMessage) {
                // Si le but est que ce message d'erreur soit toujours en bas s'il est affiché,
                // il faudrait peut-être une structure avec un Spacer(Modifier.weight(1f))
                // au-dessus de ce Text.
                Spacer(modifier = Modifier.weight(1f)) // Pousse le texte d'erreur vers le bas
                Text(
                    text = stringResource(id = R.string.magic_ball_no_interaction_method_active),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                // Un spacer pour s'assurer qu'il y a de l'espace en bas si le message d'erreur n'est pas affiché.
                // Si l'historique est vide, ce spacer sera juste après le texte principal.
                // Si l'historique est présent, il sera après l'historique.
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
            onNavigateToStats = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "MagicBallScreen Landscape")
@Composable
fun MagicBallScreenLandscapePreview() {
    DivinationAppTheme {
        MagicBallScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}
