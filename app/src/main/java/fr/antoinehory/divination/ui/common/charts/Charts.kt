package fr.antoinehory.divination.ui.common.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.antoinehory.divination.viewmodels.GameGlobalShareEntry
import fr.antoinehory.divination.viewmodels.StatItem
import fr.antoinehory.divination.ui.theme.OrakniumBackground
import fr.antoinehory.divination.ui.theme.OrakniumGold
import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface as AndroidTypeface // Explicit import for clarity

/**
 * Utility function to generate a map of colors for a list of [StatItem]s.
 * Colors are interpolated between a [startColor] and [endColor] based on the item's
 * percentage, after sorting the items by percentage in descending order.
 * This is typically used for assigning consistent colors to pie chart segments and their legends.
 *
 * @param statItems The list of [StatItem] objects to generate colors for.
 * @param startColor The color used for the item with the highest percentage (or for a single item).
 * @param endColor The color used for the item with the lowest percentage.
 * @return A map where keys are [StatItem.resultKey] and values are the calculated [Color].
 *         Returns an empty map if [statItems] is empty.
 */
fun getStatItemColors(
    statItems: List<StatItem>,
    startColor: Color,
    endColor: Color
): Map<String, Color> {
    if (statItems.isEmpty()) return emptyMap()
    val sortedItems = statItems.sortedByDescending { it.percentage }
    val colors = mutableMapOf<String, Color>()
    sortedItems.forEachIndexed { index, item ->
        val fraction = if (sortedItems.size > 1) {
            index.toFloat() / (sortedItems.size - 1).toFloat()
        } else {
            0.5f
        }
        colors[item.resultKey] = lerp(startColor, endColor, fraction)
    }
    return colors
}

/**
 * A composable that draws a pie chart based on a list of [StatItem]s.
 * Each slice of the pie represents a [StatItem], with its size proportional to [StatItem.count].
 * Colors for the slices are interpolated between [startColor] and [endColor].
 * An optional border can be drawn around the pie chart.
 *
 * @param statItems The list of [StatItem] objects to represent in the pie chart.
 * @param modifier [Modifier] to be applied to the chart's container Box.
 * @param startColor The color for the largest slice or the first in the sorted list.
 * @param endColor The color for the smallest slice or the last in the sorted list.
 * @param borderColor The color of the border drawn around the pie chart.
 * @param borderWidthDp The width of the border in Dp.
 */
@Composable
fun GameResultPieChart(
    statItems: List<StatItem>,
    modifier: Modifier = Modifier,
    startColor: Color = OrakniumGold,
    endColor: Color = OrakniumBackground,
    borderColor: Color = OrakniumGold,
    borderWidthDp: Dp = 1.5.dp
) {
    if (statItems.isEmpty()) return

    val sortedItems = statItems.sortedByDescending { it.percentage }
    val totalCount = sortedItems.sumOf { it.count }.toFloat().coerceAtLeast(0.001f)
    val density = LocalDensity.current
    val borderWidthPx = with(density) { borderWidthDp.toPx() }

    Box(modifier = modifier
        .aspectRatio(1f)
        .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var currentStartAngle = -90f

            sortedItems.forEachIndexed { index, item ->
                val sweepAngle = if (totalCount > 0f) {
                    (item.count.toFloat() / totalCount) * 360f
                } else {
                    0f
                }
                val fraction = if (sortedItems.size > 1) {
                    index.toFloat() / (sortedItems.size - 1).toFloat()
                } else { 0.5f }
                val sliceColor = lerp(startColor, endColor, fraction)

                if (sweepAngle > 0f) {
                    drawArc(
                        color = sliceColor,
                        startAngle = currentStartAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }
                currentStartAngle += sweepAngle
            }

            if (borderWidthPx > 0f) {
                drawArc(
                    color = borderColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = borderWidthPx)
                )
            }
        }
    }
}

/**
 * Composable function that draws a global distribution bar chart.
 * This chart visualizes the share of total plays for each game type within the application.
 *
 * @param chartEntries A list of [GameGlobalShareEntry] data points, where each entry
 *                     represents a game type, its display name, total plays, and share percentage.
 * @param modifier Optional [Modifier] to be applied to the Canvas where the chart is drawn.
 */
@Composable
fun GlobalDistributionChart(chartEntries: List<GameGlobalShareEntry>, modifier: Modifier = Modifier) {
    if (chartEntries.isEmpty()) {
        return
    }
    val density = LocalDensity.current
    val barColor = MaterialTheme.colorScheme.primary

    val gameNameTextColor = MaterialTheme.colorScheme.onSurface
    val gameNameTextPaint = remember(gameNameTextColor, density) {
        AndroidPaint().apply {
            color = gameNameTextColor.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
        }
    }

    val valueTextColorInBar = OrakniumBackground
    val valueTextPaint = remember(valueTextColorInBar, density) {
        AndroidPaint().apply {
            color = valueTextColorInBar.toArgb()
            textAlign = AndroidPaint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
            isAntiAlias = true
            typeface = AndroidTypeface.create(AndroidTypeface.DEFAULT, AndroidTypeface.BOLD)
        }
    }

    val bottomPaddingForGameName = with(density) { 25.dp.toPx() }
    val topPadding = with(density) { 8.dp.toPx() }
    val spacingBetweenBars = with(density) { 8.dp.toPx() }
    val valueTextMarginFromBarBottomPx = with(density) { 4.dp.toPx() }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(220.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val numBars = chartEntries.size

        val totalSpacing = spacingBetweenBars * (numBars - 1).coerceAtLeast(0)
        val barWidth = if (numBars > 0) (canvasWidth - totalSpacing) / numBars else 0f

        if (barWidth <= 0f && numBars > 0) return@Canvas

        val maxPercentage = chartEntries.maxOfOrNull { it.sharePercentage }?.takeIf { it > 0f } ?: 1f
        val chartAreaHeight = canvasHeight - topPadding - bottomPaddingForGameName

        if (chartAreaHeight <= 0f) return@Canvas

        val textBounds = android.graphics.Rect()

        chartEntries.forEachIndexed { index, entry ->
            val barHeight = (entry.sharePercentage / maxPercentage) * chartAreaHeight
            val barLeft = index * (barWidth + spacingBetweenBars)
            val barTopY = canvasHeight - bottomPaddingForGameName - barHeight
            val barBottomY = canvasHeight - bottomPaddingForGameName

            drawRect(
                color = barColor,
                topLeft = Offset(x = barLeft, y = barTopY),
                size = Size(barWidth.coerceAtLeast(0f), barHeight.coerceAtLeast(0f))
            )

            val valueText = entry.totalPlaysForGame.toString()
            valueTextPaint.getTextBounds(valueText, 0, valueText.length, textBounds)
            val valueTextHeight = textBounds.height()

            if (barHeight > valueTextHeight + (2 * valueTextMarginFromBarBottomPx)) {
                val valueTextBaselineY = barBottomY - valueTextMarginFromBarBottomPx - textBounds.bottom
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    barLeft + barWidth / 2,
                    valueTextBaselineY,
                    valueTextPaint
                )
            }

            val gameNameText = entry.gameDisplayName
            val gameNameBaselineY = canvasHeight - bottomPaddingForGameName + with(density) { 15.dp.toPx() }

            drawContext.canvas.nativeCanvas.drawText(
                gameNameText,
                barLeft + barWidth / 2,
                gameNameBaselineY,
                gameNameTextPaint
            )
        }
    }
}
