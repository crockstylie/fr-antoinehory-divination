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
import androidx.compose.ui.res.stringResource
import fr.antoinehory.divination.R
import fr.antoinehory.divination.ui.theme.OrakniumGold

/**
 * A reusable scaffold composable for application screens.
 *
 * This composable provides a consistent layout structure including a top app bar,
 * optional navigation icon, actions, bottom bar, and floating action button.
 * It also handles window insets for edge-to-edge display.
 *
 * @param title The title displayed in the [TopAppBar].
 * @param canNavigateBack Boolean indicating if the back navigation icon should be shown.
 * @param onNavigateBack Lambda function to be invoked when the navigation icon is clicked.
 * @param actions A composable lambda defining actions to be displayed in the [TopAppBar].
 *                Defaults to an empty composable.
 * @param bottomBar A composable lambda for the bottom bar content. Defaults to an empty composable.
 * @param floatingActionButton A composable lambda for the floating action button.
 *                             Defaults to an empty composable.
 * @param content A composable lambda that receives [PaddingValues] and defines the main content
 *                of the screen within the scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing), // Applies padding for system bars
        topBar = {
            TopAppBar(
                title = { Text(text = title, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    if (canNavigateBack) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.navigation_back_description),
                                tint = OrakniumGold // Custom tint for the navigation icon
                            )
                        }
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = OrakniumGold // Ensures consistent color for navigation icon
                )
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background // Sets the background color of the scaffold body
    ) { innerPadding ->
        // The main content of the screen is placed here, respecting the inner padding
        // provided by the Scaffold (e.g., for top app bar, bottom bar).
        content(innerPadding)
    }
}
