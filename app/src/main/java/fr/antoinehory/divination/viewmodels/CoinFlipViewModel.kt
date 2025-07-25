package fr.antoinehory.divination.viewmodels

import android.app.Application
import fr.antoinehory.divination.R // << AJOUTER CET IMPORT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

class CoinFlipViewModel(application: Application) : ShakeDetectViewModel(application) {

    private val app: Application = application // Instance pour accéder aux ressources

    private val _coinFace = MutableStateFlow<CoinFace?>(null)
    val coinFace: StateFlow<CoinFace?> = _coinFace

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage

    companion object {
        private const val FLIP_ANIMATION_DELAY_MS = 1000L
        // Les constantes de messages en dur sont supprimées
    }

    init {
        if (!isAccelerometerAvailable.value) {
            _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_no_accelerometer)
        } else {
            _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_shake)
        }
    }

    private fun flipCoin() {
        val isHeads = Random.nextBoolean()
        _coinFace.value = if (isHeads) CoinFace.HEADS else CoinFace.TAILS
        _currentMessage.value = if (isHeads) {
            app.getString(R.string.coin_flip_result_heads)
        } else {
            app.getString(R.string.coin_flip_result_tails)
        }
    }

    override suspend fun onShakeDetected() {
        _currentMessage.value = app.getString(R.string.coin_flip_flipping_message)
        _coinFace.value = null // Cache la pièce pendant l'animation/le délai
        delay(FLIP_ANIMATION_DELAY_MS)
        flipCoin()
        completeShakeProcessing()
    }
}

enum class CoinFace { // Cet enum reste tel quel
    HEADS, TAILS
}