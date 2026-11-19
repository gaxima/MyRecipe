package com.gaxima.myrecipe.presentation.list

import androidx.annotation.StringRes

/**
 * One-off effects (ViewModel → UI). Delivered through a `SharedFlow` without replay, so they are
 * only used for things that are fine to lose, such as a snackbar; anything that must survive
 * lives in [RecipeListUiState].
 */
sealed interface RecipeListEvent {
    data class ShowSnackbar(@StringRes val messageRes: Int) : RecipeListEvent
}
