// file: app/src/main/java/fr/antoinehory/divination/ui/theme/Shapes.kt
package fr.antoinehory.divination.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes // Ensure this import is for material3
import androidx.compose.ui.unit.dp

val Shapes = Shapes( // This should be 'val Shapes = Shapes(...)'
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(16.dp)
)
