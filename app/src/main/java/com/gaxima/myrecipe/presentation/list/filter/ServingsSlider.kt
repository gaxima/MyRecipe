package com.gaxima.myrecipe.presentation.list.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gaxima.myrecipe.R
import kotlin.math.roundToInt

const val SERVINGS_MIN = 1

/** The slider's top stop, shown as "8+". */
const val SERVINGS_MAX = 8

/**
 * Converts slider stops to the search range: the full range means "any" (`null`) and the top
 * stop is open-ended (`start..Int.MAX_VALUE`).
 */
fun servingsRangeFromSlider(start: Int, end: Int): IntRange? = when {
    start <= SERVINGS_MIN && end >= SERVINGS_MAX -> null
    end >= SERVINGS_MAX -> start..Int.MAX_VALUE
    else -> start..end
}

fun IntRange?.toSliderPosition(): ClosedFloatingPointRange<Float> {
    if (this == null) return SERVINGS_MIN.toFloat()..SERVINGS_MAX.toFloat()
    val start = first.coerceIn(SERVINGS_MIN, SERVINGS_MAX)
    val end = last.coerceIn(start, SERVINGS_MAX)
    return start.toFloat()..end.toFloat()
}

@Composable
fun ServingsSlider(
    range: IntRange?,
    onRangeChange: (IntRange?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.servings_label), style = MaterialTheme.typography.titleSmall)
            Text(range.valueLabel(), style = MaterialTheme.typography.bodyMedium)
        }
        RangeSlider(
            value = range.toSliderPosition(),
            onValueChange = { onRangeChange(servingsRangeFromSlider(it.start.roundToInt(), it.endInclusive.roundToInt())) },
            valueRange = SERVINGS_MIN.toFloat()..SERVINGS_MAX.toFloat(),
            steps = SERVINGS_MAX - SERVINGS_MIN - 1,
        )
    }
}

@Composable
private fun IntRange?.valueLabel(): String = when {
    this == null -> stringResource(R.string.servings_any)
    last == Int.MAX_VALUE -> stringResource(R.string.servings_value_min, first)
    first == last -> first.toString()
    else -> stringResource(R.string.servings_value_range, first, last)
}
