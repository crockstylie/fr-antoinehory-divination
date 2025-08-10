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
import fr.antoinehory.divination.data.database.entity.LaunchLog

class CoinFlipViewModel(
    application: Application,
    private val launchLogRepository: LaunchLogRepository
) : AndroidViewModel(application) {

    private val app: Application = application

    private val _coinFace = MutableStateFlow<CoinFace?>(null)
    val coinFace: StateFlow<CoinFace?> = _coinFace.asStateFlow()

    private val _currentMessage = MutableStateFlow("")
    val currentMessage: StateFlow<String> = _currentMessage.asStateFlow()

    private val _isFlipping = MutableStateFlow(false)
    val isFlipping: StateFlow<Boolean> = _isFlipping.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<LaunchLog>>(emptyList())
    val recentLogs: StateFlow<List<LaunchLog>> = _recentLogs.asStateFlow()

    companion object {
        private const val FLIP_ANIMATION_DELAY_MS = 1000L
    }

    init {
        _currentMessage.value = app.getString(R.string.coin_flip_initial_prompt_generic)
        viewModelScope.launch {
            // MODIFICATION ICI : Récupérer 10 lancers au lieu de 5
            launchLogRepository.getRecentLogsByGameType(GameType.COIN_FLIP, 10)
                .collect { logs ->
                    _recentLogs.value = logs
                }
        }
    }

    private fun determineFlipResult() {
        viewModelScope.launch {
            val isHeads = Random.nextBoolean()
            val resultFace = if (isHeads) CoinFace.HEADS else CoinFace.TAILS
            _coinFace.value = resultFace
            _currentMessage.value = if (isHeads) {
                app.getString(R.string.coin_flip_result_heads)
            } else {
                app.getString(R.string.coin_flip_result_tails)
            }

            launchLogRepository.insertLog(
                gameType = GameType.COIN_FLIP,
                result = resultFace.name
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
            determineFlipResult()
            _isFlipping.value = false
        }
    }
}

enum class CoinFace {
    HEADS, TAILS
}

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
