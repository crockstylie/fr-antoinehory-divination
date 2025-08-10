package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable // Keep if main column click is desired
import androidx.compose.foundation.interaction.MutableInteractionSource // Keep for clickable
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
import fr.antoinehory.divination.data.model.InteractionMode
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.GameHistoryDisplay
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.common.BottomAppNavigationBar // AJOUT: Import de la barre de navigation commune
import fr.antoinehory.divination.ui.theme.DivinationAppTheme // RESTAURÉ: Import nécessaire pour les Previews
// import fr.antoinehory.divination.ui.theme.OrakniumGold // Potentially unused now if only for old bottom bar
import fr.antoinehory.divination.viewmodels.CoinFace
import fr.antoinehory.divination.viewmodels.CoinFlipViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.CoinFlipViewModelFactory

@Composable
fun CoinFlipScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit,
    onNavigateToInfo: () -> Unit // AJOUT: Paramètre pour la navigation vers l'écran d'info
) {
    val context = LocalContext.current
    val application = context.applicationContext as DivinationApplication
    val launchLogRepository = application.launchLogRepository

    val coinFlipViewModel: CoinFlipViewModel = viewModel(
        factory = CoinFlipViewModelFactory(application, launchLogRepository)
    )

    val currentMessage by coinFlipViewModel.currentMessage.collectAsState()
    val coinFace by coinFlipViewModel.coinFace.collectAsState()
    val isFlipping by coinFlipViewModel.isFlipping.collectAsState()
    val recentLogs by coinFlipViewModel.recentLogs.collectAsState()

    val interactionPrefs by interactionViewModel.interactionPreferences.collectAsState()
    val isShakeAvailable by interactionViewModel.isShakeAvailable.collectAsState()

    LaunchedEffect(interactionViewModel, coinFlipViewModel, isFlipping) {
        interactionViewModel.interactionTriggered.collect { _event ->
            if (!isFlipping) {
                coinFlipViewModel.performCoinFlip()
            }
        }
    }

    val currentLocalContext = LocalContext.current
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
        onNavigateBack = onNavigateBack,
        bottomBar = {
            BottomAppNavigationBar(
                onSettingsClick = { /* Action pour Settings, masquée pour l'instant */ },
                onStatsClick = { onNavigateToStats(GameType.COIN_FLIP) },
                onInfoClick = onNavigateToInfo,
                showSettingsButton = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (!isFlipping) {
                            if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                                interactionViewModel.userTappedScreen()
                            }
                        }
                    }
                ),
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
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(textAlpha)
            )

            GameHistoryDisplay(
                recentLogs = recentLogs,
                gameType = GameType.COIN_FLIP
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinFlipScreenPreview() {
    DivinationAppTheme {
        CoinFlipScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "CoinFlipScreen Landscape")
@Composable
fun CoinFlipScreenLandscapePreview() {
    DivinationAppTheme {
        CoinFlipScreen(
            onNavigateBack = {},
            onNavigateToStats = {},
            onNavigateToInfo = {}
        )
    }
}

