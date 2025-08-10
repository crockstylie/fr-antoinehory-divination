package fr.antoinehory.divination.data.model;

/**
 * Defines the available modes for user interaction to trigger actions in some games.
 */
enum class InteractionMode {
    /** Interaction is triggered by shaking the device. */
    SHAKE,
    /** Interaction is triggered by tapping the screen. */
    TAP
}

/**
 * Represents the user's preferred interaction settings.
 *
 * @property activeInteractionMode The currently active [InteractionMode] selected by the user.
 *                                Defaults to [InteractionMode.TAP].
 */
data class InteractionPreferences(
    val activeInteractionMode: InteractionMode = InteractionMode.TAP
)

