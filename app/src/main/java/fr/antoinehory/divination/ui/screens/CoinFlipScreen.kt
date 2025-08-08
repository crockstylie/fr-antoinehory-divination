package fr.antoinehory.divination.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // AJOUT
import androidx.compose.foundation.verticalScroll // AJOUT
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
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.ui.common.AppScaffold
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold
import fr.antoinehory.divination.viewmodels.CoinFace
import fr.antoinehory.divination.viewmodels.CoinFlipViewModel
import fr.antoinehory.divination.viewmodels.InteractionDetectViewModel
// Imports ajoutés :
import fr.antoinehory.divination.DivinationApplication
import fr.antoinehory.divination.viewmodels.CoinFlipViewModelFactory

@Composable
fun CoinFlipScreen(
    onNavigateBack: () -> Unit,
    interactionViewModel: InteractionDetectViewModel = viewModel(),
    onNavigateToStats: (GameType) -> Unit
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
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = OrakniumGold
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onNavigateToStats(GameType.COIN_FLIP) }) {
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
                .padding(paddingValues) // Appliquer le padding fourni par AppScaffold
                .verticalScroll(rememberScrollState()) // AJOUT DU DÉFILEMENT
                .padding(16.dp) // Padding interne spécifique à cet écran (gardé après le scroll)
                .clickable {
                    if (!isFlipping) {
                        if (interactionPrefs.activeInteractionMode == InteractionMode.TAP) {
                            interactionViewModel.userTappedScreen()
                        }
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // Gardé pour centrer si le contenu est petit
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

            // Ajout d'un Spacer en bas pour un meilleur espacement en mode défilement si le contenu est grand
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
            onNavigateToStats = {}
        )
    }
}

// Ajout d'une preview paysage pour tester le défilement
@Preview(showBackground = true, widthDp = 720, heightDp = 360, name = "CoinFlipScreen Landscape")
@Composable
fun CoinFlipScreenLandscapePreview() {
    DivinationAppTheme {
        CoinFlipScreen(
            onNavigateBack = {},
            onNavigateToStats = {}
        )
    }
}
