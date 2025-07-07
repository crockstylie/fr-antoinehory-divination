package fr.antoinehory.divination

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import fr.antoinehory.divination.ui.theme.DivinationAppTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MagicBallViewModel by viewModels()
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private var lastUpdate: Long = 0
    private var last_x: Float = 0.0f
    private var last_y: Float = 0.0f
    private var last_z: Float = 0.0f
    private val shakeThreshold = 800 // Ajustez si nécessaire

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        setContent {
            DivinationAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val responseText by viewModel.currentResponse
                    val isShuffling by viewModel.isShuffling

                    MagicBallScreen(
                        responseText = responseText,
                        isShuffling = isShuffling
                    )

                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner, sensorManager, accelerometer) {
                        val listener = object : SensorEventListener {
                            override fun onSensorChanged(event: SensorEvent?) {
                                if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                                    val currentTime = System.currentTimeMillis()
                                    if ((currentTime - lastUpdate) > 100) {
                                        val diffTime = (currentTime - lastUpdate)
                                        lastUpdate = currentTime

                                        val x = event.values[0]
                                        val y = event.values[1]
                                        val z = event.values[2]

                                        val speed = kotlin.math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000

                                        if (speed > shakeThreshold) {
                                            if (!isShuffling) { // Pourrait être judicieux de ne lancer une nouvelle animation que si la précédente est finie
                                                viewModel.startShuffleAndShowResponse()
                                            }
                                        }
                                        last_x = x
                                        last_y = y
                                        last_z = z
                                    }
                                }
                            }
                            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* Non utilisé */ }
                        }

                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                accelerometer?.also { accel ->
                                    sensorManager.registerListener(listener, accel, SensorManager.SENSOR_DELAY_UI)
                                }
                            } else if (event == Lifecycle.Event.ON_PAUSE) {
                                sensorManager.unregisterListener(listener)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)

                        onDispose {
                            sensorManager.unregisterListener(listener)
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MagicBallScreen(responseText: String, isShuffling: Boolean) {
    // Animation de l'alpha (transparence)
    val textAlpha by animateFloatAsState(
        targetValue = if (isShuffling) 0.6f else 1.0f, // 60% opaque pendant le shuffle, 100% sinon
        animationSpec = tween(durationMillis = 300), // Durée de la transition d'alpha
        label = "textAlpha"
    )

    // Optionnel: Animation de la couleur du texte
    // Si vous voulez que le texte devienne explicitement blanc (ou une autre couleur) quand il n'est pas en shuffle
    val textColor by animateColorAsState(
        targetValue = if (isShuffling) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) // Couleur normale avec alpha pendant shuffle
        else MaterialTheme.colorScheme.onBackground, // Couleur normale et opaque
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val localSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (localSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null && responseText.contains("Secouez")) {
            Text(
                text = "Accéléromètre non disponible sur cet appareil.",
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = responseText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = textColor, // Utiliser la couleur animée
                modifier = Modifier.alpha(textAlpha) // Appliquer l'alpha animé
            )
        }
    }
}

// --- Previews ---
@Preview(showBackground = true, name = "Light Mode - Idle")
@Composable
fun DefaultPreviewLightIdle() {
    DivinationAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MagicBallScreen("C'est certain.", isShuffling = false)
        }
    }
}

@Preview(showBackground = true, name = "Light Mode - Shuffling")
@Composable
fun DefaultPreviewLightShuffling() {
    DivinationAppTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MagicBallScreen("Réponse brumeuse...", isShuffling = true)
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode - Idle")
@Composable
fun DefaultPreviewDarkIdle() {
    DivinationAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MagicBallScreen("Les signes pointent vers oui.", isShuffling = false)
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode - Shuffling")
@Composable
fun DefaultPreviewDarkShuffling() {
    DivinationAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MagicBallScreen("Demande à nouveau...", isShuffling = true)
        }
    }
}

@Preview(showBackground = true, name = "Initial State")
@Composable
fun InitialStatePreview() {
    DivinationAppTheme(darkTheme = false) { // ou true, peu importe pour l'état initial
        Surface(color = MaterialTheme.colorScheme.background) {
            MagicBallScreen("Secouez pour obtenir une réponse", isShuffling = false)
        }
    }
}