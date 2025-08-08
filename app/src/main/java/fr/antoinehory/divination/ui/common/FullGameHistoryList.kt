package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
// LogResultFormatter est maintenant importé implicitement car il est dans le même package (ui.common)
// et défini dans GameHistoryDisplay.kt
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun FullGameHistoryList(
    logs: List<LaunchLog>,
    logResultFormatter: LogResultFormatter, // Vient de GameHistoryDisplay.kt via GameStatsScreen
    modifier: Modifier = Modifier // Ce modifier sera appliqué au Column racine
) {
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

    // Le Column racine reçoit le modifier externe (qui devrait inclure heightIn).
    Column(modifier = modifier) {
        Text(
            text = stringResource(id = R.string.stats_full_history_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp)
        )
        // La LazyColumn interne ne NÉCESSITE PAS heightIn ici si son parent Column
        // est déjà contraint en hauteur par le 'modifier' externe.
        // fillMaxWidth() est suffisant.
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(logs, key = { it.id }) { log -> // Utiliser log.id comme clé
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(log.timestamp) // Format de date ajusté
                val formattedResult = logResultFormatter.format(log.result, log.gameType)

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(0.4f)
                        )
                        Text(
                            text = formattedResult,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .weight(0.6f)
                                .padding(start = 8.dp),
                            textAlign = TextAlign.End
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                }
            }
        }
    }
}
