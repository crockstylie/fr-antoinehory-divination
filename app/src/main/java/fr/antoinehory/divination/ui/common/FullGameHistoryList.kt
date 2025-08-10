package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.database.entity.LaunchLog
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A composable that displays a full list of game history logs ([LaunchLog]).
 * Each item in the list shows the formatted date of the game and the formatted result.
 * If the list of logs is empty, a message indicating no history is available is shown.
 *
 * @param logs The list of [LaunchLog] items to display.
 * @param logResultFormatter An instance of [LogResultFormatter] used to format the game result string.
 *                           (Assuming [LogResultFormatter] is an interface or class responsible for formatting).
 * @param modifier [Modifier] to be applied to the Column containing the list. Defaults to [Modifier].
 */
@Composable
fun FullGameHistoryList(
    logs: List<LaunchLog>,
    logResultFormatter: LogResultFormatter, // Assuming LogResultFormatter is a defined interface/class
    modifier: Modifier = Modifier
) {
    // Display a message if no history is available
    if (logs.isEmpty()) {
        Text(
            text = stringResource(id = R.string.stats_no_history_available),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    Column(modifier = modifier) {
        // Title for the full history list
        Text(
            text = stringResource(id = R.string.stats_full_history_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
        )
        // Scrollable list of game logs
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(logs, key = { it.id }) { log ->
                // Format the timestamp to a human-readable date and time
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(log.timestamp)
                // Format the game result using the provided formatter
                val formattedResult = logResultFormatter.format(log.result, log.gameType)

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the formatted date
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.4f)
                        )
                        // Display the formatted game result
                        Text(
                            text = formattedResult,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(start = 8.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    // Visual separator between items
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}
