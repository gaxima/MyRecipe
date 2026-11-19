package com.gaxima.myrecipe.presentation.list.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.RecipeSearchParams

/** Trimmed, lowercased ingredient name, or `null` when there is nothing to add. */
fun normalizeIngredient(raw: String): String? = raw.trim().lowercase().takeIf { it.isNotEmpty() }

/** Adds an ingredient to the include list, removing it from exclude so the search stays satisfiable. */
fun RecipeSearchParams.withIncluded(raw: String): RecipeSearchParams {
    val name = normalizeIngredient(raw) ?: return this
    return copy(includeIngredients = includeIngredients + name, excludeIngredients = excludeIngredients - name)
}

/** Adds an ingredient to the exclude list, removing it from include so the search stays satisfiable. */
fun RecipeSearchParams.withExcluded(raw: String): RecipeSearchParams {
    val name = normalizeIngredient(raw) ?: return this
    return copy(excludeIngredients = excludeIngredients + name, includeIngredients = includeIngredients - name)
}

/** Commits ingredient text the user typed but did not add yet (no Done / + pressed). */
fun RecipeSearchParams.withPendingIngredients(include: String, exclude: String): RecipeSearchParams =
    withIncluded(include).withExcluded(exclude)

/**
 * Text field that turns each entry into a removable chip via the keyboard's Done action or the
 * trailing add button. The caller owns [text] so pending input can be committed on Apply.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientInput(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    ingredients: Set<String>,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.ingredient_hint)) },
            trailingIcon = {
                if (text.isNotBlank()) {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add_ingredient))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAdd() }),
            modifier = Modifier.fillMaxWidth(),
        )
        if (ingredients.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ingredients.forEach { ingredient ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(ingredient) },
                        label = { Text(ingredient) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.cd_remove_ingredient, ingredient),
                                modifier = Modifier.size(InputChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }
        }
    }
}
