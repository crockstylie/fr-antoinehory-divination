package fr.antoinehory.divination

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import fr.antoinehory.divination.navigation.AppNavigation // Import de votre fonction de navigation centralisée
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DivinationAppTheme {
                val navController = rememberNavController()
                // Appel à votre fonction de navigation centralisée
                AppNavigation(navController = navController)
            }
        }
    }
}