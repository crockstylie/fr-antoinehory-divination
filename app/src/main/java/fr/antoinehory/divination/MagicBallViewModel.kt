package fr.antoinehory.divination

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MagicBallViewModel : ViewModel() {

    private val _magicBallResponses = listOf(
        // ... (votre liste de réponses reste la même)
        "C'est certain.", "C'est décidément ainsi.", "Sans aucun doute.", "Oui, définitivement.",
        "Tu peux compter dessus.", "Très probable.", "Les perspectives sont bonnes.", "Oui.",
        "Les signes pointent vers oui.",
        "Réponse brumeuse, essaie à nouveau.", "Demande à nouveau plus tard.",
        "Mieux vaut ne pas te le dire maintenant.", "Impossible à prédire pour le moment.",
        "Concentre-toi et demande à nouveau.",
        "Ne compte pas dessus.", "Ma réponse est non.", "Mes sources disent non.",
        "Les perspectives ne sont pas si bonnes.", "Très peu probable."
    )

    private val _currentResponse = mutableStateOf("Secouez pour obtenir une réponse")
    val currentResponse: State<String> = _currentResponse

    private val _isShuffling = mutableStateOf(false)
    val isShuffling: State<Boolean> = _isShuffling

    private var shuffleJob: Job? = null

    fun startShuffleAndShowResponse() {
        shuffleJob?.cancel() // Annule une animation précédente si elle existe
        shuffleJob = viewModelScope.launch {
            _isShuffling.value = true
            val shuffleDuration = 1500L // Durée totale du défilement en millisecondes
            val changeInterval = 80L  // À quelle fréquence changer le mot pendant le défilement

            var elapsedTime = 0L
            while (elapsedTime < shuffleDuration) {
                _currentResponse.value = _magicBallResponses.random()
                delay(changeInterval)
                elapsedTime += changeInterval
            }

            // Sélection finale après le défilement
            _currentResponse.value = _magicBallResponses.random()
            _isShuffling.value = false
        }
    }
}