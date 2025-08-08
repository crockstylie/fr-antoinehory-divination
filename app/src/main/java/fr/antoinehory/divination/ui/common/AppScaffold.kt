package fr.antoinehory.divination.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource // AJOUT : Pour la description de l'icône de retour
import fr.antoinehory.divination.R // AJOUT : Pour R.string.navigation_back_description
import fr.antoinehory.divination.ui.theme.OrakniumGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {}, // AJOUT : Paramètre pour le FAB
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                // MODIFIÉ : Utilisation d'une ressource string pour l'accessibilité
                                contentDescription = stringResource(id = R.string.navigation_back_description),
                                tint = OrakniumGold
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = OrakniumGold
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton, // AJOUT : Passage du FAB au Scaffold interne
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        content(innerPadding)
    }
}