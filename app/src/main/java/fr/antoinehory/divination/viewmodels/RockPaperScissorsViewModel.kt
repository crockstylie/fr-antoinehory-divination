package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel // Changement de l'héritage
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RPSOutcome { ROCK, PAPER, SCISSORS }

class RockPaperScissorsViewModel(application: Application) : AndroidViewModel(application) { // Plus d'héritage

    private val app: Application = application

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _rpsOutcome = MutableStateFlow<RPSOutcome?>(null)
    val rpsOutcome: StateFlow<RPSOutcome?> = _rpsOutcome.asStateFlow()

    // Nouvel état pour gérer l'animation de "jeu en cours"
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    companion object {
        private const val PROCESSING_DELAY_MS = 700L
    }

    init {
        initializeRPSState(app.getString(R.string.rps_initial_prompt_generic))
        // Vous devrez ajouter R.string.rps_initial_prompt_generic : "Interagissez pour jouer !"
    }

    private fun initializeRPSState(initialMessage: String) {
        _rpsOutcome.value = null
        _currentMessage.value = initialMessage
        _isProcessing.value = false // S'assurer que le traitement est réinitialisé
    }

    private fun determineRPSOutcome() {
        val choices = RPSOutcome.entries // Ou RPSOutcome.values() si entries n'est pas dispo (anciennes versions de Kotlin)
        val randomOutcome = choices[Random.nextInt(choices.size)]

        _rpsOutcome.value = randomOutcome
        _currentMessage.value = when (randomOutcome) {
            RPSOutcome.ROCK -> app.getString(R.string.rps_result_rock)
            RPSOutcome.PAPER -> app.getString(R.string.rps_result_paper)
            RPSOutcome.SCISSORS -> app.getString(R.string.rps_result_scissors)
        }
    }

    // Fonction publique que l'UI appellera
    fun playGame() {
        if (_isProcessing.value) return // Empêcher les appels multiples

        viewModelScope.launch {
            _isProcessing.value = true
            _currentMessage.value = app.getString(R.string.rps_processing_message)
            _rpsOutcome.value = null // Cache l'icône pendant le traitement
            delay(PROCESSING_DELAY_MS) // Simule le "jeu"
            determineRPSOutcome()
            _isProcessing.value = false
        }
    }

    // Optionnel: si vous voulez une fonction explicite pour réinitialiser depuis l'UI un jour
    // fun resetGameExplicitly() {
    //     initializeRPSState(app.getString(R.string.rps_initial_prompt_generic))
    // }
}

