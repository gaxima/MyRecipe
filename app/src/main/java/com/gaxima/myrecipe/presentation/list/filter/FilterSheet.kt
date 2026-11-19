package com.gaxima.myrecipe.presentation.list.filter

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.presentation.list.SearchParamsStateSaver
import com.gaxima.myrecipe.presentation.list.clearedFilters
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme

/**
 * Bottom sheet that edits a draft copy of [initialParams]. Only **Apply** publishes the draft;
 * dismissing the sheet discards it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(
    initialParams: RecipeSearchParams,
    onApply: (RecipeSearchParams) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable(stateSaver = SearchParamsStateSaver) { mutableStateOf(initialParams) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        FilterSheetContent(
            draft = draft,
            onDraftChange = { draft = it },
            onApply = { onApply(draft) },
            onReset = { draft = draft.clearedFilters() },
            modifier = Modifier.imePadding(),
        )
    }
}

/** Stateless sheet body; the caller owns [draft]. */
@Composable
fun FilterSheetContent(
    draft: RecipeSearchParams,
    onDraftChange: (RecipeSearchParams) -> Unit,
    onApply: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(stringResource(R.string.filters_title), style = MaterialTheme.typography.titleLarge)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = draft.vegetarianOnly,
                    role = Role.Switch,
                    onValueChange = { onDraftChange(draft.copy(vegetarianOnly = it)) },
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.filter_vegetarian_only), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.filter_vegetarian_only_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = draft.vegetarianOnly, onCheckedChange = null)
        }

        ServingsSlider(
            range = draft.servingsRange,
            onRangeChange = { onDraftChange(draft.copy(servingsRange = it)) },
        )

        IngredientInput(
            label = stringResource(R.string.include_label),
            ingredients = draft.includeIngredients,
            onAdd = { onDraftChange(draft.withIncluded(it)) },
            onRemove = { onDraftChange(draft.copy(includeIngredients = draft.includeIngredients - it)) },
        )

        IngredientInput(
            label = stringResource(R.string.exclude_label),
            ingredients = draft.excludeIngredients,
            onAdd = { onDraftChange(draft.withExcluded(it)) },
            onRemove = { onDraftChange(draft.copy(excludeIngredients = draft.excludeIngredients - it)) },
        )

        OutlinedTextField(
            value = draft.instructionQuery,
            onValueChange = { onDraftChange(draft.copy(instructionQuery = it)) },
            label = { Text(stringResource(R.string.instructions_label)) },
            placeholder = { Text(stringResource(R.string.instructions_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            TextButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
            Button(onClick = onApply) { Text(stringResource(R.string.apply)) }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FilterSheetContentPreview() {
    MyRecipeTheme(dynamicColor = false) {
        Surface {
            FilterSheetContent(
                draft = RecipeSearchParams(
                    vegetarianOnly = true,
                    servingsRange = 2..4,
                    includeIngredients = setOf("rice", "garlic"),
                    excludeIngredients = setOf("peanut"),
                    instructionQuery = "oven",
                ),
                onDraftChange = {},
                onApply = {},
                onReset = {},
            )
        }
    }
}
