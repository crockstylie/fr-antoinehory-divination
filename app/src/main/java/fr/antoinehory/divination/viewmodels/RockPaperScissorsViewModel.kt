package fr.antoinehory.divination.viewmodels

import android.app.Application
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

enum class RPSOutcome { ROCK, PAPER, SCISSORS }

class RockPaperScissorsViewModel(application: Application) : ShakeDetectViewModel(application) {
    private val app: Application = application
    // private val TAG = "RPSViewModel" // Commenté ou supprimé

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    private val _rpsOutcome = MutableStateFlow<RPSOutcome?>(null)
    val rpsOutcome: StateFlow<RPSOutcome?> = _rpsOutcome

    companion object {
        private const val PROCESSING_DELAY_MS = 700L
    }

    init {
        // Log.d(TAG, "init - Accelerometer available: ${isAccelerometerAvailable.value}")
        initializeRPSState()
    }

    fun resetGame() {
        // Log.d(TAG, "resetGame called")
        initializeRPSState()
    }

    private fun initializeRPSState() {
        _rpsOutcome.value = null
        val initialMsg = if (isAccelerometerAvailable.value) {
            app.getString(R.string.rps_initial_prompt_shake)
        } else {
            app.getString(R.string.rps_initial_prompt_no_accelerometer)
        }
        _currentMessage.value = initialMsg
        // Log.d(TAG, "initializeRPSState - Outcome: ${_rpsOutcome.value}, Message: $initialMsg")
    }

    private suspend fun playRPS() {
        // Log.d(TAG, "playRPS called")
        val choices = RPSOutcome.entries
        val randomOutcome = choices[Random.nextInt(choices.size)]
        // Log.d(TAG, "playRPS - Random outcome chosen: $randomOutcome")

        _rpsOutcome.value = randomOutcome
        val resultMsg = when (randomOutcome) {
            RPSOutcome.ROCK -> app.getString(R.string.rps_result_rock)
            RPSOutcome.PAPER -> app.getString(R.string.rps_result_paper)
            RPSOutcome.SCISSORS -> app.getString(R.string.rps_result_scissors)
        }
        _currentMessage.value = resultMsg
        // Log.d(TAG, "playRPS - Final Outcome: ${_rpsOutcome.value}, Final Message: $resultMsg")
    }

    override suspend fun onShakeDetected() {
        // Log.d(TAG, "onShakeDetected - START. Current isProcessingShake (from super): ${isProcessingShake.value}")

        _currentMessage.value = app.getString(R.string.rps_processing_message)
        _rpsOutcome.value = null // Cache l'icône pendant le traitement
        // Log.d(TAG, "onShakeDetected - Set to processing. Outcome: ${_rpsOutcome.value}, Message: ${_currentMessage.value}")

        delay(PROCESSING_DELAY_MS) // Simule le "jeu"
        // Log.d(TAG, "onShakeDetected - Delay finished.")

        playRPS() // Détermine et affiche le nouveau résultat
        // Log.d(TAG, "onShakeDetected - playRPS finished. Outcome: ${_rpsOutcome.value}, Message: ${_currentMessage.value}")

        completeShakeProcessing()
        // Log.d(TAG, "onShakeDetected - completeShakeProcessing() CALLED. isProcessingShake should be false now.")
    }
}
