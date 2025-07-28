// Fichier : TapDetectViewModel.kt
package fr.antoinehory.divination.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// --- IMPORTS AJOUTÉS ---
import fr.antoinehory.divination.viewmodels.InteractionEvent // Importer l'interface
import fr.antoinehory.divination.viewmodels.TapEvent         // Importer l'objet TapEvent défini globalement

// La définition locale de TapEvent est SUPPRIMÉE d'ici :
// object TapEvent : InteractionEvent // <-- SUPPRIMER CETTE LIGNE

class TapDetectViewModel(application: Application) : AndroidViewModel(application) {

    // Flow pour émettre les événements de tapotement.
    // Le type <InteractionEvent> est maintenant correctement résolu grâce à l'import.
    private val _interactionDetected = MutableSharedFlow<InteractionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val interactionDetected: SharedFlow<InteractionEvent> = _interactionDetected.asSharedFlow()

    // Flag local pour un cooldown si nécessaire (actuellement commenté, ce qui est bien si géré globalement)
    // private var isProcessingTap = false
    // private val tapCooldownMs = 200L

    fun onScreenTapped() {
        // Optionnel : cooldown local
        // if (!isProcessingTap) {
        //     isProcessingTap = true

        // Émet l'objet TapEvent importé de InteractionEvent.kt
        _interactionDetected.tryEmit(TapEvent)

        //     viewModelScope.launch { // viewModelScope n'est pas directement disponible sans import et coroutine builder
        //         delay(tapCooldownMs)
        //         isProcessingTap = false
        //     }
        // }
    }

    fun completeInteractionProcessing() {
        // Réinitialiser tout état spécifique au tap si nécessaire
        // isProcessingTap = false // Si vous avez un cooldown local
    }

    fun registerListener() {
        // Rien à faire pour le tap initié par l'UI.
    }

    fun unregisterListener() {
        // Rien à faire.
    }

    override fun onCleared() {
        super.onCleared()
        // Nettoyer les ressources si nécessaire.
    }
}
