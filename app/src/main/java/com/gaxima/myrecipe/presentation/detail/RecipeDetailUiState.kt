package com.gaxima.myrecipe.presentation.detail

import androidx.annotation.StringRes
import com.gaxima.myrecipe.domain.model.Recipe

sealed interface RecipeDetailUiState {
    data object Loading : RecipeDetailUiState
    data class Success(val recipe: Recipe) : RecipeDetailUiState
    data class Error(@StringRes val messageRes: Int) : RecipeDetailUiState
}

sealed interface RecipeDetailAction {
    data object Retry : RecipeDetailAction
}
