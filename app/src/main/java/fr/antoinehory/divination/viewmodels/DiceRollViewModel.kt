package fr.antoinehory.divination.viewmodels

import android.app.Application
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class DiceRollViewModel(application: Application) : ShakeDetectViewModel(application) {

    private val app: Application = application

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    // Stocke la valeur du dé (1 à 6), ou null si pas encore lancé ou pendant le traitement
    private val _diceValue = MutableStateFlow<Int?>(null)
    val diceValue: StateFlow<Int?> = _diceValue

    companion object {
        private const val ROLLING_DELAY_MS = 700L // Délai pour simuler le lancer
        private const val DICE_SIDES = 6
    }

    init {
        initializeDiceState()
    }

    fun resetGame() {
        initializeDiceState()
    }

    private fun initializeDiceState() {
        _diceValue.value = null
        _currentMessage.value = if (isAccelerometerAvailable.value) {
            app.getString(R.string.dice_initial_prompt_shake)
        } else {
            // Tu pourrais avoir un message différent ou une logique de bouton ici
            app.getString(R.string.dice_initial_prompt_no_accelerometer)
        }
    }

    private suspend fun rollDice() {
        // Simuler le choix aléatoire d'une face du dé
        val randomDiceValue = Random.nextInt(1, DICE_SIDES + 1) // De 1 à DICE_SIDES inclus

        _diceValue.value = randomDiceValue
        _currentMessage.value = app.getString(R.string.dice_result_format, randomDiceValue)
    }

    override suspend fun onShakeDetected() {
        _currentMessage.value = app.getString(R.string.dice_rolling_message)
        _diceValue.value = null // Cache la face du dé pendant le "lancement"

        delay(ROLLING_DELAY_MS)

        rollDice() // Lance le dé et met à jour les états

        completeShakeProcessing() // Crucial pour que ShakeDetectViewModel permette de nouvelles secousses
    }

    // Ajout pour implémenter le membre abstrait de SensorEventListener hérité via ShakeDetectViewModel
    override fun onAccuracyChanged(sensor: android.hardware.Sensor?, accuracy: Int) {
        // Pas nécessaire pour cette implémentation, peut être laissé vide.
    }
}
