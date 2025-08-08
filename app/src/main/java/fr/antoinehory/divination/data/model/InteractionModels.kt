package fr.antoinehory.divination.data.model;

// Ajout d'un enum pour représenter le mode d'interaction actif
enum class InteractionMode {
    SHAKE,
    TAP
}

data class InteractionPreferences(
    // On pourrait garder isShakeDetectionEnabled et isTapDetectionEnabled
    // et s'assurer qu'un seul est vrai dans le ViewModel,
    // OU utiliser un seul champ pour le mode actif.
    // Utilisons un seul champ pour plus de clarté dans ce cas.
    val activeInteractionMode: InteractionMode = InteractionMode.TAP // TAP par défaut
)
