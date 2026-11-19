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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

/**
 * Text field that turns each entry into a removable chip when the keyboard's Done action is pressed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IngredientInput(
    label: String,
    ingredients: Set<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by rememberSaveable { mutableStateOf("") }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(label) },
            placeholder = { Text(stringResource(R.string.ingredient_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    onAdd(text)
                    text = ""
                },
            ),
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
