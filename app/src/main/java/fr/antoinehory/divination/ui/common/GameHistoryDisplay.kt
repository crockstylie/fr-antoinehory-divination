package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext // NÉCESSAIRE pour defaultLogResultFormatter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.RPSOutcome

// Interface pour la transformation du résultat du log en chaîne affichable
fun interface LogResultFormatter {
    @Composable
    fun format(logResult: String, gameType: GameType): String
}

@Composable
fun defaultLogResultFormatter(): LogResultFormatter {
    val context = LocalContext.current // Récupérer le contexte ici
    return LogResultFormatter { logResult, gameType ->
        when (gameType) {
            GameType.DICE_ROLL -> stringResource(R.string.dice_result_format, logResult.toIntOrNull() ?: 0)
            GameType.COIN_FLIP -> {
                when (logResult) {
                    "HEADS" -> stringResource(id = R.string.coin_flip_result_heads)
                    "TAILS" -> stringResource(id = R.string.coin_flip_result_tails)
                    else -> logResult
                }
            }
            GameType.MAGIC_EIGHT_BALL -> {
                val resources = context.resources
                val index = logResult.toIntOrNull()
                if (index != null) {
                    val possibleAnswers = resources.getStringArray(R.array.magic_ball_possible_answers)
                    possibleAnswers.getOrNull(index) ?: logResult // Retourne l'index comme chaîne si hors limites
                } else if (logResult == "FALLBACK") {
                    // Utiliser la chaîne de fallback définie ou une chaîne générique si la première est vide
                    val fallbackDisplay = resources.getString(R.string.magic_ball_default_answer_if_empty)
                    if (fallbackDisplay.isNotBlank()) fallbackDisplay else context.getString(R.string.stats_fallback_answer_display)
                } else {
                    logResult // Si ce n'est ni un index ni "FALLBACK", retourne la chaîne brute
                }
            }
            GameType.ROCK_PAPER_SCISSORS -> {
                when (logResult) {
                    RPSOutcome.ROCK.name -> stringResource(R.string.rps_result_rock)
                    RPSOutcome.PAPER.name -> stringResource(R.string.rps_result_paper)
                    RPSOutcome.SCISSORS.name -> stringResource(R.string.rps_result_scissors)
                    else -> logResult
                }
            }
        }
    }
}

@Composable
fun GameHistoryDisplay( // Ce composant est pour l'historique RÉCENT (écrans de jeu)
    recentLogs: List<LaunchLog>,
    gameType: GameType,
    modifier: Modifier = Modifier,
    logResultFormatter: LogResultFormatter = defaultLogResultFormatter()
) {
    if (recentLogs.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            val maxLogsForStyleEffect = recentLogs.size.coerceAtLeast(1)

            recentLogs.forEachIndexed { index, log ->
                val itemAlpha = 1.0f - (index.toFloat() / maxLogsForStyleEffect.toFloat() * 0.7f).coerceIn(0f, 0.8f)
                val itemSize = ((20 - (index * 1.0f).coerceAtMost(10.0f))).sp
                val resultDisplay = logResultFormatter.format(log.result, gameType)

                Text(
                    text = resultDisplay,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = itemSize),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(itemAlpha)
                        .padding(vertical = 1.dp)
                )
            }
        }
    }
}
