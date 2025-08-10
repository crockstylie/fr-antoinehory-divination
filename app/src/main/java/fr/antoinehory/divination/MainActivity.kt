package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.divination.navigation.AppNavigation
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

/**
 * The main activity for the Divination application.
 * This activity serves as the entry point and hosts the Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     * This is where you should do all of your normal static set up: create views,
     * bind data to lists, etc. This method also provides you with a Bundle containing
     * the activity's previously frozen state, if there was one.
     *
     * It enables edge-to-edge display and sets up the main content view using Jetpack Compose,
     * initializing the [DivinationAppTheme] and [AppNavigation].
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display for a more immersive user experience.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Set the content of the activity to be a Composable function.
        setContent {
            // Apply the application's custom theme.
            DivinationAppTheme {
                // rememberNavController() creates and remembers a NavController
                // for navigating between composables.
                val navController = rememberNavController()
                // AppNavigation sets up the navigation graph for the application.
                AppNavigation(navController = navController)
            }
        }
    }
}