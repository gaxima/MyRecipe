package com.gaxima.myrecipe.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Pops [key] only while it is still on top. During the pop transition the exiting screen stays
 * composed and clickable, so a quick second tap on its back button must not pop the screen
 * underneath it. The root entry is never removed.
 */
fun MutableList<NavKey>.popIfTop(key: NavKey) {
    if (size > 1 && lastOrNull() == key) removeAt(lastIndex)
}

/** Opens a recipe only from the list, so a double tap on a card cannot push two detail entries. */
fun MutableList<NavKey>.openRecipe(recipeId: String) {
    if (lastOrNull() == RecipeList) add(RecipeDetail(recipeId))
}
