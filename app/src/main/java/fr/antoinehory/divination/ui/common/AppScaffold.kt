package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.antoinehory.divination.ui.theme.OrakniumGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = OrakniumGold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Fond de la TopAppBar
                    titleContentColor = MaterialTheme.colorScheme.onBackground, // Couleur du titre
                    navigationIconContentColor = OrakniumGold // Couleur de l'icône de navigation
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Fond du Scaffold lui-même
    ) { innerPadding ->
        // Passe le padding interne au contenu pour qu'il ne soit pas sous la TopAppBar
        content(innerPadding)
    }
}

