package com.gaxima.myrecipe.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R

/** Removable chips for the applied filters; tapping a chip removes that filter and searches again. */
@Composable
fun ActiveFilterChips(
    filters: List<ActiveFilter>,
    onRemove: (ActiveFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(filters, key = { it.toString() }) { filter ->
            val label = filter.label()
            InputChip(
                selected = true,
                onClick = { onRemove(filter) },
                label = { Text(label) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_remove_filter, label),
                        modifier = Modifier.size(InputChipDefaults.IconSize),
                    )
                },
            )
        }
    }
}

@Composable
private fun ActiveFilter.label(): String = when (this) {
    ActiveFilter.Vegetarian -> stringResource(R.string.filter_vegetarian)
    is ActiveFilter.Servings -> when {
        range.last == Int.MAX_VALUE -> stringResource(R.string.filter_servings_min, range.first)
        range.first == range.last -> pluralStringResource(R.plurals.servings_count, range.first, range.first)
        else -> stringResource(R.string.filter_servings_range, range.first, range.last)
    }
    is ActiveFilter.Include -> stringResource(R.string.filter_include, ingredient)
    is ActiveFilter.Exclude -> stringResource(R.string.filter_exclude, ingredient)
    is ActiveFilter.Instructions -> stringResource(R.string.filter_instructions, text)
}
