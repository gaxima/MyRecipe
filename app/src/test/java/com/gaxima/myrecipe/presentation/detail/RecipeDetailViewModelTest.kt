package com.gaxima.myrecipe.presentation.detail

import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.testutil.FakeRecipeRepository
import com.gaxima.myrecipe.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class RecipeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val recipe = PreviewData.vegetarianRecipe
    private val repository = FakeRecipeRepository().apply { recipeResult = Result.success(recipe) }

    private fun viewModel(id: String = recipe.id) = RecipeDetailViewModel(id, repository)

    @Test
    fun `shows loading while the recipe is fetched`() {
        repository.recipeGate = CompletableDeferred()

        assertEquals(RecipeDetailUiState.Loading, viewModel().uiState.value)
    }

    @Test
    fun `loads recipe on init`() {
        val vm = viewModel()

        assertEquals(RecipeDetailUiState.Success(recipe), vm.uiState.value)
        assertEquals(listOf(recipe.id), repository.recipeCalls)
    }

    @Test
    fun `unknown id shows not-found error`() {
        repository.recipeResult = Result.failure(RecipeNotFoundException("missing"))

        val vm = viewModel("missing")

        assertEquals(RecipeDetailUiState.Error(R.string.error_recipe_not_found), vm.uiState.value)
    }

    @Test
    fun `network failure shows network error`() {
        repository.recipeResult = Result.failure(IOException())

        assertEquals(RecipeDetailUiState.Error(R.string.error_network), viewModel().uiState.value)
    }

    @Test
    fun `retry reloads`() {
        repository.recipeResult = Result.failure(IOException())
        val vm = viewModel()

        repository.recipeResult = Result.success(recipe)
        vm.onAction(RecipeDetailAction.Retry)

        assertEquals(RecipeDetailUiState.Success(recipe), vm.uiState.value)
        assertEquals(2, repository.recipeCalls.size)
    }

    @Test
    fun `retry shows loading first`() {
        repository.recipeResult = Result.failure(IOException())
        val vm = viewModel()

        repository.recipeGate = CompletableDeferred()
        vm.onAction(RecipeDetailAction.Retry)

        assertEquals(RecipeDetailUiState.Loading, vm.uiState.value)
    }
}
