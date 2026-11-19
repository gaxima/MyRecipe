package com.gaxima.myrecipe.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.gaxima.myrecipe.presentation.detail.RecipeDetailScreen
import com.gaxima.myrecipe.presentation.list.RecipeListScreen
import kotlinx.serialization.Serializable

@Serializable
data object RecipeList : NavKey

/** Carries only the id: the detail screen loads its own data, which also keeps deep links possible. */
@Serializable
data class RecipeDetail(val recipeId: String) : NavKey

@Composable
fun RecipeNavigation(modifier: Modifier = Modifier) {
    // Saved across configuration changes and process death (keys are @Serializable).
    val backStack = rememberNavBackStack(RecipeList)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Gives each entry its own ViewModelStore, cleared when the entry is popped.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<RecipeList> {
                RecipeListScreen(onRecipeClick = { id -> backStack.add(RecipeDetail(id)) })
            }
            entry<RecipeDetail> { key ->
                RecipeDetailScreen(recipeId = key.recipeId, onBack = { backStack.removeLastOrNull() })
            }
        },
    )
}
