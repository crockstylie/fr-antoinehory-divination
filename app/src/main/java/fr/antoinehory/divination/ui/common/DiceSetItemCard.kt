package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.BorderStroke
// import androidx.compose.foundation.layout.Arrangement // Non utilisé, peut être retiré si pas utilisé ailleurs
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy // NOUVEL IMPORT
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.DiceConfig
import fr.antoinehory.divination.data.model.DiceSet
import fr.antoinehory.divination.data.model.DiceType
import fr.antoinehory.divination.ui.theme.DivinationAppTheme
import fr.antoinehory.divination.ui.theme.OrakniumGold

@Composable
fun DiceSetItemCard(
    diceSet: DiceSet,
    onLaunch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit, // NOUVEAU PARAMÈTRE
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, OrakniumGold),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = diceSet.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OrakniumGold
            )
            Text(
                text = diceSet.summaryDisplay, // Assurez-vous que ce champ existe et est bien rempli dans votre modèle DiceSet
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                color = OrakniumGold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = if (diceSet.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(if (diceSet.isFavorite) R.string.dice_set_remove_from_favorites else R.string.dice_set_add_to_favorites),
                        tint = OrakniumGold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Row {
                    // NOUVELLE IconButton pour COPIER
                    IconButton(onClick = onCopy, modifier = Modifier.size(44.dp)) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(R.string.copy_dice_set_desc),
                            tint = OrakniumGold // Ou MaterialTheme.colorScheme.primary si vous préférez
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp)) // Espace entre Copier et Modifier
                    IconButton(onClick = onEdit, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.dice_set_edit_set_desc),
                            tint = OrakniumGold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.dice_set_delete_set_desc),
                            tint = OrakniumGold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(onClick = onLaunch, modifier = Modifier.size(44.dp)) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.dice_set_launch_set_desc),
                            tint = OrakniumGold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DiceSetItemCard Preview")
@Composable
fun DiceSetItemCardPreview() {
    val previewSet = DiceSet(
        id = 1,
        name = "Carte de Test",
        diceConfigs = listOf(
            DiceConfig(diceType = DiceType.D20, count = 1),
            DiceConfig(diceType = DiceType.D6, count = 2)
        ),
        isFavorite = true
    )
    DivinationAppTheme {
        DiceSetItemCard(
            diceSet = previewSet,
            onLaunch = { },
            onToggleFavorite = { },
            onEdit = { },
            onDelete = { },
            onCopy = { } // AJOUTÉ pour le Preview
        )
    }
}

@Preview(showBackground = true, name = "DiceSetItemCard Not Favorite Preview")
@Composable
fun DiceSetItemCardNotFavoritePreview() {
    val previewSet = DiceSet(
        id = 2,
        name = "Autre Carte",
        diceConfigs = listOf(DiceConfig(diceType = DiceType.D10, count = 3)),
        isFavorite = false
    )
    DivinationAppTheme {
        DiceSetItemCard(
            diceSet = previewSet,
            onLaunch = { },
            onToggleFavorite = { },
            onEdit = { },
            onDelete = { },
            onCopy = { } // AJOUTÉ pour le Preview
        )
    }
}