package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.R
import fr.antoinehory.divination.data.model.GameType
import fr.antoinehory.divination.data.repository.LaunchLogRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class CoinFlipViewModel(
    application: Application,
    private val launchLogRepository: LaunchLogRepository // Ajout du repository
) : AndroidViewModel(application) {

    private val app: Application = application

    private val _coinFace = MutableStateFlow<CoinFace?>(null)
    val coinFace: StateFlow<CoinFace?> = _coinFace.asStateFlow()

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _isFlipping = MutableStateFlow(false)
    val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

    companion object {
        private const val FLIP_ANIMATION_DELAY_MS = 1000L
    }

    init {
        _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_generic)
    }

    private fun determineFlipResult() {
        viewModelScope.launch { // Lancer dans une coroutine pour l'appel suspendu à insertLog
            val isHeads = Random.nextBoolean()
            val resultFace = if (isHeads) CoinFace.HEADS else CoinFace.TAILS
            _coinFace.value = resultFace
            _currentMessage.value = if (isHeads) {
                app.getString(R.string.coin_flip_result_heads)
            } else {
                app.getString(R.string.coin_flip_result_tails)
            }

            // Enregistrer le résultat
            launchLogRepository.insertLog(
                gameType = GameType.COIN_FLIP,
                result = resultFace.name // Enregistre "HEADS" ou "TAILS"
            )
        }
    }

    fun performCoinFlip() {
        if (_isFlipping.value) return

        viewModelScope.launch {
            _isFlipping.value = true
            _currentMessage.value = app.getString(R.string.coin_flip_flipping_message)
            _coinFace.value = null
            delay(FLIP_ANIMATION_DELAY_MS)
            // determineFlipResult est déjà dans un viewModelScope.launch,
            // mais l'appel lui-même n'a pas besoin d'être dans un nouveau launch ici.
            // Cependant, determineFlipResult() contient maintenant un appel suspendu,
            // donc il doit être appelé depuis une coroutine ou être une fonction suspendue elle-même.
            // Pour l'instant, appelons-la directement, son contenu est déjà lancé.
            determineFlipResult() // Appel modifié pour être dans le scope
            _isFlipping.value = false
        }
    }
}

enum class CoinFace {
    HEADS, TAILS
}

// Factory pour CoinFlipViewModel
class CoinFlipViewModelFactory(
    private val application: Application,
    private val launchLogRepository: LaunchLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CoinFlipViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CoinFlipViewModel(application, launchLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
