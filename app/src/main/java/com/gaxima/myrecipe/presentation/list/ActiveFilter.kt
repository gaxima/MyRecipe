package com.gaxima.myrecipe.presentation.list

import com.gaxima.myrecipe.domain.model.RecipeSearchParams

/** One removable filter chip. The free-text query is not a chip: it is visible in the search field. */
sealed interface ActiveFilter {
    data object Vegetarian : ActiveFilter
    data class Servings(val range: IntRange) : ActiveFilter
    data class Include(val ingredient: String) : ActiveFilter
    data class Exclude(val ingredient: String) : ActiveFilter
    data class Instructions(val text: String) : ActiveFilter
}

fun RecipeSearchParams.toActiveFilters(): List<ActiveFilter> = buildList {
    if (vegetarianOnly) add(ActiveFilter.Vegetarian)
    servingsRange?.let { add(ActiveFilter.Servings(it)) }
    includeIngredients.forEach { add(ActiveFilter.Include(it)) }
    excludeIngredients.forEach { add(ActiveFilter.Exclude(it)) }
    instructionQuery.trim().takeIf { it.isNotEmpty() }?.let { add(ActiveFilter.Instructions(it)) }
}

fun RecipeSearchParams.without(filter: ActiveFilter): RecipeSearchParams = when (filter) {
    ActiveFilter.Vegetarian -> copy(vegetarianOnly = false)
    is ActiveFilter.Servings -> copy(servingsRange = null)
    is ActiveFilter.Include -> copy(includeIngredients = includeIngredients - filter.ingredient)
    is ActiveFilter.Exclude -> copy(excludeIngredients = excludeIngredients - filter.ingredient)
    is ActiveFilter.Instructions -> copy(instructionQuery = "")
}

/** Removes every filter but keeps the query. */
fun RecipeSearchParams.clearedFilters(): RecipeSearchParams = RecipeSearchParams(query = query)
