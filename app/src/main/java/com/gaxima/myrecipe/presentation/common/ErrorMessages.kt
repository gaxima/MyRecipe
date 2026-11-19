package com.gaxima.myrecipe.presentation.common

import androidx.annotation.StringRes
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import java.io.IOException

/** Maps a failure to the message shown to the user. Shared by every screen. */
@StringRes
fun Throwable.toUserMessage(): Int = when (this) {
    is RecipeNotFoundException -> R.string.error_recipe_not_found
    is IOException -> R.string.error_network
    else -> R.string.error_generic
}
