// Fichier: InteractionDetectViewModel.kt
package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import fr.antoinehory.divination.data.InteractionMode // <-- NOUVEL IMPORT
import fr.antoinehory.divination.data.InteractionPreferences
import fr.antoinehory.divination.data.UserPreferencesRepository
import fr.antoinehory.divination.viewmodels.InteractionEvent // Assurez-vous que cet import est correct
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InteractionDetectViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferencesRepository = UserPreferencesRepository(application.applicationContext)

    // Instances des détecteurs spécifiques
    private val shakeDetector = ShakeDetectViewModel(application)
    private val tapDetector = TapDetectViewModel(application)

    val interactionPreferences: StateFlow<InteractionPreferences> =
        userPreferencesRepository.interactionPreferencesFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = InteractionPreferences() // Par défaut, InteractionMode.TAP
            )

    private val _interactionTriggered = MutableSharedFlow<InteractionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val interactionTriggered: SharedFlow<InteractionEvent> = _interactionTriggered.asSharedFlow()

    private var shakeJob: Job? = null
    private var tapJob: Job? = null
    // private var blowJob: Job? = null // Supprimé car la fonctionnalité de souffle est enlevée

    private val _isShakeAvailable = MutableStateFlow(false)
    val isShakeAvailable: StateFlow<Boolean> = _isShakeAvailable.asStateFlow()

    // Tap est toujours considéré comme disponible, pas besoin d'un StateFlow spécifique pour sa disponibilité
    // val isTapAvailable: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow() // Peut être supprimé si non utilisé ailleurs

    // Supprimé car la fonctionnalité de souffle est enlevée
    // private val _isMicrophoneAvailable = MutableStateFlow(false)
    // val isMicrophoneAvailable: StateFlow<Boolean> = _isMicrophoneAvailable.asStateFlow()
    // private val _isRecordAudioPermissionGranted = MutableStateFlow(false)
    // val isRecordAudioPermissionGranted: StateFlow<Boolean> = _isRecordAudioPermissionGranted.asStateFlow()

    private var isProcessingGlobalInteraction = false
    private val globalInteractionCooldownMs = 500L

    init {
        // Collecter la disponibilité du matériel depuis le détecteur de secousse
        viewModelScope.launch {
            shakeDetector.isAccelerometerAvailable.collect { available ->
                _isShakeAvailable.value = available
                // Si le mode "Secouer" était actif et que l'accéléromètre devient indisponible,
                // basculer vers le mode "Taper" par défaut.
                if (!available && interactionPreferences.value.activeInteractionMode == InteractionMode.SHAKE) {
                    setActiveInteractionMode(InteractionMode.TAP)
                }
            }
        }

        // Observer les préférences et mettre à jour les listeners
        viewModelScope.launch {
            interactionPreferences.collect { prefs ->
                updateListenersBasedOnPreferences(prefs)
            }
        }
    }

    private fun updateListenersBasedOnPreferences(prefs: InteractionPreferences) {
        // --- Shake Detector ---
        if (prefs.activeInteractionMode == InteractionMode.SHAKE && _isShakeAvailable.value) {
            if (shakeJob == null || shakeJob?.isActive == false) {
                shakeDetector.registerListener()
                shakeJob = viewModelScope.launch {
                    shakeDetector.interactionDetected.collect { event ->
                        handleDetectedInteraction(event, shakeDetector::completeInteractionProcessing)
                    }
                }
            }
        } else {
            shakeJob?.cancel()
            shakeJob = null
            shakeDetector.unregisterListener()
        }

        // --- Tap Detector ---
        if (prefs.activeInteractionMode == InteractionMode.TAP) {
            if (tapJob == null || tapJob?.isActive == false) {
                tapDetector.registerListener() // Même si vide, pour cohérence
                tapJob = viewModelScope.launch {
                    tapDetector.interactionDetected.collect { event ->
                        handleDetectedInteraction(event, tapDetector::completeInteractionProcessing)
                    }
                }
            }
        } else {
            tapJob?.cancel()
            tapJob = null
            tapDetector.unregisterListener() // Même si vide
        }
    }

    private suspend fun handleDetectedInteraction(event: InteractionEvent, onProcessedCallback: () -> Unit) {
        if (!isProcessingGlobalInteraction) {
            isProcessingGlobalInteraction = true
            _interactionTriggered.tryEmit(event) // `val emitted` n'était pas utilisé

            onProcessedCallback()

            delay(globalInteractionCooldownMs)
            isProcessingGlobalInteraction = false
        } else {
            onProcessedCallback()
        }
    }

    // --- Fonction pour l'UI (SettingsScreen) pour mettre à jour les préférences via Repository ---
    fun setActiveInteractionMode(mode: InteractionMode) {
        viewModelScope.launch {
            // Empêcher la sélection de SHAKE si l'accéléromètre n'est pas disponible
            if (mode == InteractionMode.SHAKE && !_isShakeAvailable.value) {
                // Optionnel: Vous pourriez émettre un événement à l'UI pour informer l'utilisateur,
                // mais l'UI devrait déjà désactiver l'option.
                return@launch
            }
            userPreferencesRepository.updateActiveInteractionMode(mode)
        }
    }

    // Appelée par l'UI (écrans de jeu) pour signaler un tap manuel,
    // mais le TapDetectViewModel gère maintenant sa propre logique de détection de tap
    // via l'interaction orchestrée.
    // Cependant, si vous avez un bouton explicite "TAPPER" dans un jeu, cette fonction reste utile.
    fun userTappedScreen() {
        // Le TapDetectViewModel écoutera les taps si InteractionMode.TAP est actif.
        // Cette fonction signale explicitement un tap au TapDetectViewModel,
        // qui devrait alors émettre son événement si TAP est le mode actif.
        tapDetector.onScreenTapped() // Cette méthode dans TapDetectViewModel devrait vérifier en interne si elle doit émettre
        // ou simplement enregistrer le tap pour une analyse future si nécessaire.
        // Ou, plus simplement, InteractionDetectViewModel pourrait vérifier ici:
        // if (interactionPreferences.value.activeInteractionMode == InteractionMode.TAP) {
        // tapDetector.onScreenTapped()
        // }
        // La solution actuelle où TapDetectViewModel gère l'émission est probablement meilleure pour la séparation des préoccupations.
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.unregisterListener()
        tapDetector.unregisterListener()
        shakeJob?.cancel()
        tapJob?.cancel()
        // blowJob?.cancel() // Supprimé
    }
}

