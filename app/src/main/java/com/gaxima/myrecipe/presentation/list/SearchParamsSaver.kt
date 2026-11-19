package com.gaxima.myrecipe.presentation.list

import androidx.compose.runtime.saveable.Saver
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal const val SEARCH_PARAMS_KEY = "search_params"

/** For `rememberSaveable(stateSaver = SearchParamsStateSaver)`: keeps a draft across rotation. */
internal val SearchParamsStateSaver: Saver<RecipeSearchParams, String> = Saver(
    save = { it.encode() },
    restore = { decodeSearchParams(it) },
)

/**
 * Bundle-friendly form of [RecipeSearchParams] for `SavedStateHandle` / `rememberSaveable`.
 * A presentation-side surrogate keeps the domain model free of serialization concerns.
 */
@Serializable
private data class SavedSearchParams(
    val query: String = "",
    val vegetarianOnly: Boolean = false,
    val minServings: Int? = null,
    val maxServings: Int? = null,
    val includeIngredients: List<String> = emptyList(),
    val excludeIngredients: List<String> = emptyList(),
    val instructionQuery: String = "",
)

internal fun RecipeSearchParams.encode(): String = Json.encodeToString(
    SavedSearchParams(
        query = query,
        vegetarianOnly = vegetarianOnly,
        minServings = servingsRange?.first,
        maxServings = servingsRange?.last,
        includeIngredients = includeIngredients.toList(),
        excludeIngredients = excludeIngredients.toList(),
        instructionQuery = instructionQuery,
    ),
)

/** @throws kotlinx.serialization.SerializationException when [value] is not an encoded params string. */
internal fun decodeSearchParams(value: String): RecipeSearchParams {
    val saved = Json.decodeFromString<SavedSearchParams>(value)
    val min = saved.minServings
    val max = saved.maxServings
    return RecipeSearchParams(
        query = saved.query,
        vegetarianOnly = saved.vegetarianOnly,
        servingsRange = if (min != null && max != null) min..max else null,
        includeIngredients = saved.includeIngredients.toSet(),
        excludeIngredients = saved.excludeIngredients.toSet(),
        instructionQuery = saved.instructionQuery,
    )
}
