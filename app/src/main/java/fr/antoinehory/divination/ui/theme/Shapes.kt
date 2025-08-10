package fr.antoinehory.divination.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Defines the default shapes for components in the application, following Material Design guidelines.
 * These shapes are used by Material 3 components to define their corner styles.
 *
 * - `small`: Used for smaller components like chips, buttons.
 * - `medium`: Used for medium-sized components like cards.
 * - `large`: Used for larger components like bottom sheets, dialogs.
 */
val Shapes = Shapes(
    /** Defines a small rounded corner shape with a 4dp radius. */
    small = RoundedCornerShape(4.dp),
    /** Defines a medium rounded corner shape with an 8dp radius. */
    medium = RoundedCornerShape(8.dp),
    /** Defines a large rounded corner shape with a 16dp radius. */
    large = RoundedCornerShape(16.dp)
)

