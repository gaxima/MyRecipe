package com.gaxima.myrecipe.presentation.list

import com.gaxima.myrecipe.domain.model.RecipeSearchParams

/** Everything the user can do on the recipe list screen (UI → ViewModel). */
sealed interface RecipeListAction {
    data class SearchTextChange(val text: String) : RecipeListAction

    /** IME "Search" pressed: searches with the typed text. */
    data object Search : RecipeListAction
    data object ClearSearch : RecipeListAction

    /** Applies the filter sheet's draft; the current query is kept. */
    data class ApplyFilters(val params: RecipeSearchParams) : RecipeListAction
    data class RemoveFilter(val filter: ActiveFilter) : RecipeListAction

    /** Resets the query and every filter. */
    data object ClearAllFilters : RecipeListAction
    data object Retry : RecipeListAction
}
