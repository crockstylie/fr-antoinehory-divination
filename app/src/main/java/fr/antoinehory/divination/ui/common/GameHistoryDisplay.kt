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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.RPSOutcome // Pour l'exemple de RPS

// Interface pour la transformation du résultat du log en chaîne affichable
fun interface LogResultFormatter {
    @Composable
    fun format(logResult: String, gameType: GameType): String
}

@Composable
fun defaultLogResultFormatter(): LogResultFormatter { // Correction: camelCase ici
    return LogResultFormatter { logResult, gameType ->
        when (gameType) {
            GameType.DICE_ROLL -> stringResource(R.string.dice_result_format, logResult.toIntOrNull() ?: 0)
            GameType.COIN_FLIP -> {
                when (logResult) {
                    // Assurez-vous que ces clés existent et sont correctes pour Pile ou Face
                    "HEADS" -> stringResource(id = R.string.coin_flip_result_heads)
                    "TAILS" -> stringResource(id = R.string.coin_flip_result_tails)
                    else -> logResult
                }
            }
            GameType.MAGIC_EIGHT_BALL -> {
                // Pour la Boule Magique, si le logResult est déjà la réponse complète et localisée,
                // alors un formateur direct n'est peut-être pas nécessaire et on pourrait
                // directement utiliser logResult. Sinon, il faudrait une logique ici pour
                // mapper les clés de réponse de la boule magique à des R.string si elles sont stockées ainsi.
                // Par exemple, si log.result est "ANSWER_YES", on ferait:
                // stringResource(id = context.resources.getIdentifier(logResult, "string", context.packageName))
                // Pour l'instant, on suppose que logResult EST la réponse affichable.
                logResult
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
fun GameHistoryDisplay(
    recentLogs: List<LaunchLog>,
    gameType: GameType,
    modifier: Modifier = Modifier,
    logResultFormatter: LogResultFormatter = defaultLogResultFormatter() // Correction: camelCase ici aussi
) {
    if (recentLogs.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            val maxLogsForStyleEffect = recentLogs.size.coerceAtLeast(1)

            recentLogs.forEachIndexed { index, log ->
                // Logique de style pour l'alpha et la taille du texte
                // (identique à celle que nous avons validée pour DiceRollScreen)
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