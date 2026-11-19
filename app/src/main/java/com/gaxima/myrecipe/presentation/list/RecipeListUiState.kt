package com.gaxima.myrecipe.presentation.list

import androidx.annotation.StringRes
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams

/**
 * @property searchText what is typed in the search field; only searched on the IME action.
 * @property appliedParams the search the current [results] belong to.
 * @property isSearching a search is in flight while previous [RecipeResultsState.Success] stays visible.
 */
data class RecipeListUiState(
    val searchText: String = "",
    val appliedParams: RecipeSearchParams = RecipeSearchParams(),
    val results: RecipeResultsState = RecipeResultsState.Loading,
    val isSearching: Boolean = false,
) {
    /** Drives the removable chips and the filter badge, so neither can drift from [appliedParams]. */
    val activeFilters: List<ActiveFilter> get() = appliedParams.toActiveFilters()
}

sealed interface RecipeResultsState {
    data object Loading : RecipeResultsState
    data class Success(val recipes: List<Recipe>) : RecipeResultsState
    data class Error(@StringRes val messageRes: Int) : RecipeResultsState
}
