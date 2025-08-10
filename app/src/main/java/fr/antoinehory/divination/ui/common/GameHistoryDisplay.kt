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
import fr.antoinehory.divination.viewmodels.RPSOutcome

/**
 * A functional interface for formatting a game log result string based on the game type.
 * Implementations should provide a composable function that takes the raw log result and game type
 * and returns a localized, user-friendly string representation.
 */
fun interface LogResultFormatter {
    /**
     * Formats the raw game log result into a displayable string.
     *
     * @param logResult The raw result string from the [LaunchLog].
     * @param gameType The [GameType] of the game associated with the log.
     * @return A [String] formatted for display in the UI. This string may be localized.
     */
    @Composable
    fun format(logResult: String, gameType: GameType): String
}

/**
 * Provides a default implementation of [LogResultFormatter].
 * This composable function localizes and formats game results for known [GameType]s:
 * - [GameType.DICE_ROLL]: Returns the result as is.
 * - [GameType.COIN_FLIP]: Translates "HEADS" and "TAILS" to localized strings.
 * - [GameType.MAGIC_EIGHT_BALL]: Looks up the answer from `R.array.magic_ball_possible_answers`
 *   if the result is an index, or uses a fallback string for "FALLBACK".
 * - [GameType.ROCK_PAPER_SCISSORS]: Translates "ROCK", "PAPER", "SCISSORS" to localized strings.
 * - Other game types: Returns the result as is.
 *
 * @return An instance of [LogResultFormatter] with default formatting logic.
 */
@Composable
fun defaultLogResultFormatter(): LogResultFormatter {
    val context = LocalContext.current
    return LogResultFormatter { logResult, gameType ->
        when (gameType) {
            GameType.DICE_ROLL -> {
                logResult
            }
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
                    possibleAnswers.getOrNull(index) ?: logResult
                } else if (logResult == "FALLBACK") {
                    val fallbackDisplay = resources.getString(R.string.magic_ball_default_answer_if_empty)
                    if (fallbackDisplay.isNotBlank()) fallbackDisplay else context.getString(R.string.stats_fallback_answer_display)
                } else {
                    logResult
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
            else -> logResult // Includes GameType.DICE_SET if it exists
        }
    }
}

/**
 * A composable function that displays a list of recent game logs for a specific game type.
 * It applies a visual effect where older logs are smaller and more transparent.
 * If the list of recent logs is empty, this composable will not render any visible output
 * other than what its parent might render.
 *
 * @param recentLogs A list of [LaunchLog] objects representing recent game plays, ordered by recency.
 * @param gameType The [GameType] for which the logs are being displayed. This is used by the [logResultFormatter].
 * @param modifier [Modifier] to be applied to the Column containing the logs. Defaults to [Modifier].
 * @param logResultFormatter An instance of [LogResultFormatter] used to format the result string of each log.
 *                           Defaults to [defaultLogResultFormatter].
 */
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
            // Ensure at least 1 to avoid division by zero or NaN issues with very small lists.
            val maxLogsForStyleEffect = recentLogs.size.coerceAtLeast(1)

            recentLogs.forEachIndexed { index, log ->
                val resultDisplay = logResultFormatter.format(log.result, gameType)

                // Calculate alpha: fades out towards the end of the list.
                // Max reduction of 0.7f, so last item will be 0.3f alpha if list is long enough.
                // Clamped between 0f (fully transparent) and 0.8f (max transparency effect).
                val itemAlpha = 1.0f - (index.toFloat() / maxLogsForStyleEffect.toFloat() * 0.7f).coerceIn(0f, 0.8f)

                // Calculate size: reduces font size for older items.
                // Max reduction of 10sp. Minimum size around 10sp if list is long.
                val itemSize = ((20 - (index * 1.0f).coerceAtMost(10.0f))).sp

                Text(
                    text = resultDisplay,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = itemSize),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .alpha(itemAlpha)
                        .padding(vertical = 10.dp) // Maintain consistent vertical spacing
                )
            }
        }
    }
}

