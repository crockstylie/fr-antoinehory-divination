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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.viewmodels.RPSOutcome // Ré-inclusion de l'import, en supposant qu'il est défini dans votre projet

fun interface LogResultFormatter {
    @Composable
    fun format(logResult: String, gameType: GameType): String
}

@Composable
fun defaultLogResultFormatter(): LogResultFormatter {
    val context = LocalContext.current
    return LogResultFormatter { logResult, gameType ->
        // Ce 'when' doit retourner une String.
        when (gameType) {
            GameType.DICE_ROLL -> {
                // Afficher directement le logResult pour les dés, car il contient déjà le résumé formaté.
                logResult
            }
            GameType.COIN_FLIP -> {
                when (logResult) {
                    "HEADS" -> stringResource(id = R.string.coin_flip_result_heads)
                    "TAILS" -> stringResource(id = R.string.coin_flip_result_tails)
                    else -> logResult // Fallback si le format change
                }
            }
            // Utilisation de GameType.MAGIC_EIGHT_BALL comme dans votre code original
            GameType.MAGIC_EIGHT_BALL -> {
                val resources = context.resources
                val index = logResult.toIntOrNull()
                if (index != null) {
                    val possibleAnswers = resources.getStringArray(R.array.magic_ball_possible_answers)
                    possibleAnswers.getOrNull(index) ?: logResult
                } else if (logResult == "FALLBACK") {
                    val fallbackDisplay = resources.getString(R.string.magic_ball_default_answer_if_empty)
                    if (fallbackDisplay.isNotBlank()) fallbackDisplay else context.getString(R.string.stats_fallback_answer_display)
                } else {
                    logResult
                }
            }
            // Utilisation de GameType.ROCK_PAPER_SCISSORS comme dans votre code original
            GameType.ROCK_PAPER_SCISSORS -> {
                // S'assurer que RPSOutcome est bien un enum et que logResult correspond à .name
                // Si RPSOutcome.ROCK.name, etc. sont les valeurs stockées dans logResult:
                when (logResult) {
                    RPSOutcome.ROCK.name -> stringResource(R.string.rps_result_rock)
                    RPSOutcome.PAPER.name -> stringResource(R.string.rps_result_paper)
                    RPSOutcome.SCISSORS.name -> stringResource(R.string.rps_result_scissors)
                    // Si vous stockez directement "ROCK", "PAPER", "SCISSORS" :
                    // "ROCK" -> stringResource(R.string.rps_result_rock)
                    // "PAPER" -> stringResource(R.string.rps_result_paper)
                    // "SCISSORS" -> stringResource(R.string.rps_result_scissors)
                    else -> logResult // Fallback si le format change
                }
            }
            // Branche 'else' pour rendre le 'when' exhaustif et garantir un retour String
            else -> logResult
        }
    }
}

@Composable
fun GameHistoryDisplay(
    recentLogs: List<LaunchLog>,
    gameType: GameType,
    modifier: Modifier = Modifier,
    logResultFormatter: LogResultFormatter = defaultLogResultFormatter()
) {
    if (recentLogs.isNotEmpty()) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.recent_activity_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxWidth()
        ) {
            val maxLogsForStyleEffect = recentLogs.size.coerceAtLeast(1)

            recentLogs.forEachIndexed { index, log ->
                // Ici, nous utilisons le gameType de l'écran actuel pour le formattage,
                // comme dans votre code original. Cela suppose que ce GameHistoryDisplay
                // est spécifique à un type de jeu (par exemple, sur l'écran DiceRoll, gameType sera DICE_ROLL).
                // Si les logs eux-mêmes peuvent être de types différents de 'gameType',
                // vous pourriez envisager d'utiliser log.gameType dans l'appel à format :
                // val resultDisplay = logResultFormatter.format(log.result, log.gameType)
                // Pour l'instant, je conserve votre logique originale :
                val resultDisplay = logResultFormatter.format(log.result, gameType)

                val itemAlpha = 1.0f - (index.toFloat() / maxLogsForStyleEffect.toFloat() * 0.7f).coerceIn(0f, 0.8f)
                val itemSize = ((20 - (index * 1.0f).coerceAtMost(10.0f))).sp

                Text(
                    text = resultDisplay,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = itemSize),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(itemAlpha)
                        .padding(vertical = 10.dp) // MODIFIED HERE
                )
            }
        }
    }
}
