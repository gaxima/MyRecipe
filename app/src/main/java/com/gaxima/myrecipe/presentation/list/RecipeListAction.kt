package com.gaxima.myrecipe.presentation.list

import com.gaxima.myrecipe.domain.model.RecipeSearchParams

/** Everything the user can do on the recipe list screen (UI → ViewModel). */
sealed interface RecipeListAction {
    data class SearchTextChange(val text: String) : RecipeListAction

    /** IME "Search" pressed: searches with the typed text. */
    data object Search : RecipeListAction
    /** ✕ pressed: empties the field, and searches again only if a query was applied. */
    data object ClearSearch : RecipeListAction

    /** Applies the filter sheet's draft together with the text currently in the search field. */
    data class ApplyFilters(val params: RecipeSearchParams) : RecipeListAction

    /** Removes one filter chip, searching with the text currently in the search field. */
    data class RemoveFilter(val filter: ActiveFilter) : RecipeListAction

    /** Resets the query and every filter. */
    data object ClearAllFilters : RecipeListAction
    data object Retry : RecipeListAction
}
