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

class CoinFlipViewModel(application: Application) : AndroidViewModel(application) { // Plus d'héritage

    private val app: Application = application

    private val _coinFace = MutableStateFlow<CoinFace?>(null)
    val coinFace: StateFlow<CoinFace?> = _coinFace.asStateFlow()

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    // Nouvel état pour gérer l'animation de "lancement en cours"
    private val _isFlipping = MutableStateFlow(false)
    val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

    companion object {
        private const val FLIP_ANIMATION_DELAY_MS = 1000L
    }

    init {
        // Message initial simple
        _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_generic)
        // Vous devrez ajouter R.string.coin_flip_initial_prompt_generic : "Interagissez pour lancer la pièce."
    }

    private fun determineFlipResult() {
        val isHeads = Random.nextBoolean()
        _coinFace.value = if (isHeads) CoinFace.HEADS else CoinFace.TAILS
        _currentMessage.value = if (isHeads) {
            app.getString(R.string.coin_flip_result_heads)
        } else {
            app.getString(R.string.coin_flip_result_tails)
        }
    }

    // Fonction publique que l'UI appellera
    fun performCoinFlip() {
        if (_isFlipping.value) return // Empêcher les appels multiples

        viewModelScope.launch {
            _isFlipping.value = true
            _currentMessage.value = app.getString(R.string.coin_flip_flipping_message)
            _coinFace.value = null // Cache la pièce pendant l'animation
            delay(FLIP_ANIMATION_DELAY_MS)
            determineFlipResult()
            _isFlipping.value = false
        }
    }
}

enum class CoinFace {
    HEADS, TAILS
}

