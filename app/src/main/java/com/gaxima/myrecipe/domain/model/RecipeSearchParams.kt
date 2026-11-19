package com.gaxima.myrecipe.domain.model

/**
 * Everything the user is currently searching for. The default instance means "no filters".
 *
 * @property servingsRange inclusive range; `null` means any. An upper bound of [Int.MAX_VALUE]
 * means open-ended ("6+").
 */
data class RecipeSearchParams(
    val query: String = "",
    val vegetarianOnly: Boolean = false,
    val servingsRange: IntRange? = null,
    val includeIngredients: Set<String> = emptySet(),
    val excludeIngredients: Set<String> = emptySet(),
    val instructionQuery: String = "",
)
