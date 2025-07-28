package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel // <-- CHANGEMENT D'HÉRITAGE
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow // <-- AJOUTÉ SI MANQUANT
import kotlinx.coroutines.launch
import kotlin.random.Random

class DiceRollViewModel(application: Application) : AndroidViewModel(application) { // <-- PLUS D'HÉRITAGE DE ShakeDetectViewModel

    private val app: Application = application

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _diceValue = MutableStateFlow<Int?>(null)
    val diceValue: StateFlow<Int?> = _diceValue.asStateFlow()

    // Nouvel état pour gérer le "lancement en cours"
    private val _isRolling = MutableStateFlow(false)
    val isRolling: StateFlow<Boolean> = _isRolling.asStateFlow()

    companion object {
        private const val ROLLING_DELAY_MS = 700L
        private const val DICE_SIDES = 6
    }

    init {
        // Le message initial sera plus générique, InteractionDetectViewModel/Screen gérera les spécificités
        initializeDiceState(app.getString(R.string.dice_initial_prompt_generic))
        // Vous devrez ajouter cette chaîne : R.string.dice_initial_prompt_generic par exemple "Interagissez pour lancer le dé !"
    }

    private fun initializeDiceState(initialMessage: String) {
        _diceValue.value = null
        _currentMessage.value = initialMessage
        _isRolling.value = false // Important de réinitialiser cet état aussi
    }

    private fun determineRollOutcome() { // Renommé de rollDice pour clarté
        val randomDiceValue = Random.nextInt(1, DICE_SIDES + 1)
        _diceValue.value = randomDiceValue
        _currentMessage.value = app.getString(R.string.dice_result_format, randomDiceValue)
    }

    // Fonction publique que l'UI appellera
    fun performRoll() {
        if (_isRolling.value) return // Empêche les lancements multiples

        viewModelScope.launch {
            _isRolling.value = true
            _currentMessage.value = app.getString(R.string.dice_rolling_message)
            _diceValue.value = null // Cache la face du dé

            delay(ROLLING_DELAY_MS)

            determineRollOutcome() // Lance le dé et met à jour les états
            _isRolling.value = false
        }
    }

    // La fonction resetGame() peut être conservée si vous avez un besoin explicite de l'appeler depuis l'UI
    // sinon, la logique d'initialisation est déjà dans init et performRoll gère son propre cycle.
    // Si vous la gardez, elle devrait utiliser le message générique :
    fun resetGameToGenericPrompt() {
        initializeDiceState(app.getString(R.string.dice_initial_prompt_generic))
    }

    // onAccuracyChanged() et onShakeDetected() sont supprimés car plus d'héritage de ShakeDetectViewModel
}

