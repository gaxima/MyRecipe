package com.gaxima.myrecipe.presentation.list

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.testutil.FakeRecipeRepository
import com.gaxima.myrecipe.testutil.MainDispatcherRule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class RecipeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val pasta = PreviewData.vegetarianRecipe
    private val curry = PreviewData.recipes[1]

    private val repository = FakeRecipeRepository().apply {
        searchResult = Result.success(listOf(pasta, curry))
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        RecipeListViewModel(repository, savedStateHandle)

    private fun RecipeListViewModel.results() = uiState.value.results

    private fun success(vararg recipes: Recipe) = RecipeResultsState.Success(recipes.toList())

    @Test
    fun `initial load searches with default params and shows success`() {
        val vm = viewModel()

        assertEquals(listOf(RecipeSearchParams()), repository.searchCalls)
        assertEquals(success(pasta, curry), vm.results())
        assertFalse(vm.uiState.value.isSearching)
    }

    @Test
    fun `initial state is loading while the first search is in flight`() {
        repository.searchGate = CompletableDeferred()

        val vm = viewModel()

        assertEquals(RecipeResultsState.Loading, vm.results())
    }

    @Test
    fun `typing does not search`() {
        val vm = viewModel()

        vm.onAction(RecipeListAction.SearchTextChange("pas"))

        assertEquals("pas", vm.uiState.value.searchText)
        assertEquals(1, repository.searchCalls.size)
    }

    @Test
    fun `search action uses trimmed typed text`() {
        val vm = viewModel()

        vm.onAction(RecipeListAction.SearchTextChange("  pasta "))
        vm.onAction(RecipeListAction.Search)

        assertEquals("pasta", repository.searchCalls.last().query)
        assertEquals("pasta", vm.uiState.value.appliedParams.query)
    }

    @Test
    fun `clear search resets text and searches`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.SearchTextChange("pasta"))
        vm.onAction(RecipeListAction.Search)

        vm.onAction(RecipeListAction.ClearSearch)

        assertEquals("", vm.uiState.value.searchText)
        assertEquals("", repository.searchCalls.last().query)
        assertEquals(3, repository.searchCalls.size)
    }

    @Test
    fun `clear search without a searched query only clears the text`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.SearchTextChange("pas"))

        vm.onAction(RecipeListAction.ClearSearch)

        assertEquals("", vm.uiState.value.searchText)
        assertEquals(1, repository.searchCalls.size)
    }

    @Test
    fun `apply filters uses the typed text even when search was not pressed`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.SearchTextChange(" curry "))

        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true)))

        assertEquals(RecipeSearchParams(query = "curry", vegetarianOnly = true), repository.searchCalls.last())
    }

    @Test
    fun `remove filter uses the typed text even when search was not pressed`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true)))
        vm.onAction(RecipeListAction.SearchTextChange("curry"))

        vm.onAction(RecipeListAction.RemoveFilter(ActiveFilter.Vegetarian))

        assertEquals(RecipeSearchParams(query = "curry"), repository.searchCalls.last())
    }

    @Test
    fun `apply filters keeps current query`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.SearchTextChange("pasta"))
        vm.onAction(RecipeListAction.Search)

        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(query = "ignored", vegetarianOnly = true)))

        assertEquals(RecipeSearchParams(query = "pasta", vegetarianOnly = true), repository.searchCalls.last())
    }

    @Test
    fun `remove filter searches without it`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true, excludeIngredients = setOf("peanut"))))

        vm.onAction(RecipeListAction.RemoveFilter(ActiveFilter.Vegetarian))

        assertEquals(RecipeSearchParams(excludeIngredients = setOf("peanut")), repository.searchCalls.last())
    }

    @Test
    fun `clear all filters resets query and filters`() {
        val vm = viewModel()
        vm.onAction(RecipeListAction.SearchTextChange("pasta"))
        vm.onAction(RecipeListAction.Search)
        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true)))

        vm.onAction(RecipeListAction.ClearAllFilters)

        assertEquals(RecipeSearchParams(), repository.searchCalls.last())
        assertEquals("", vm.uiState.value.searchText)
    }

    @Test
    fun `initial failure shows error state`() {
        repository.searchResult = Result.failure(IOException())

        val vm = viewModel()

        assertEquals(RecipeResultsState.Error(R.string.error_network), vm.results())
    }

    @Test
    fun `retry after error reloads`() {
        repository.searchResult = Result.failure(IOException())
        val vm = viewModel()

        repository.searchResult = Result.success(listOf(curry))
        vm.onAction(RecipeListAction.Retry)

        assertEquals(success(curry), vm.results())
    }

    @Test
    fun `retry from error shows loading`() {
        repository.searchResult = Result.failure(IOException())
        val vm = viewModel()

        repository.searchGate = CompletableDeferred()
        vm.onAction(RecipeListAction.Retry)

        assertEquals(RecipeResultsState.Loading, vm.results())
    }

    @Test
    fun `failure with results on screen keeps results and emits snackbar`() = runTest {
        val vm = viewModel()

        vm.events.test {
            repository.searchResult = Result.failure(IOException())
            vm.onAction(RecipeListAction.Search)

            assertEquals(RecipeListEvent.ShowSnackbar(R.string.error_network), awaitItem())
        }
        assertEquals(success(pasta, curry), vm.results())
        assertFalse(vm.uiState.value.isSearching)
    }

    @Test
    fun `failed refresh restores the params of the results on screen`() {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)
        repository.searchResult = Result.failure(IOException())

        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true)))

        assertEquals(RecipeSearchParams(), vm.uiState.value.appliedParams)
        assertEquals(emptyList<ActiveFilter>(), vm.uiState.value.activeFilters)
        assertEquals(RecipeSearchParams(), decodeSearchParams(handle[SEARCH_PARAMS_KEY]!!))
    }

    @Test
    fun `failed refresh keeps the typed text so the user can retry`() {
        val vm = viewModel()
        repository.searchResult = Result.failure(IOException())

        vm.onAction(RecipeListAction.SearchTextChange("pasta"))
        vm.onAction(RecipeListAction.Search)

        assertEquals("pasta", vm.uiState.value.searchText)
        assertEquals("", vm.uiState.value.appliedParams.query)
    }

    @Test
    fun `isSearching is true while a search is in flight with results shown`() {
        val vm = viewModel()
        val gate = CompletableDeferred<Unit>()
        repository.searchGate = gate

        vm.onAction(RecipeListAction.Search)

        assertTrue(vm.uiState.value.isSearching)
        assertEquals(success(pasta, curry), vm.results())

        gate.complete(Unit)

        assertFalse(vm.uiState.value.isSearching)
    }

    @Test
    fun `new search cancels the in-flight one`() {
        val vm = viewModel()
        val staleGate = CompletableDeferred<Unit>()
        repository.searchGate = staleGate
        repository.searchResult = Result.success(listOf(pasta))
        vm.onAction(RecipeListAction.Search)

        repository.searchGate = null
        repository.searchResult = Result.success(listOf(curry))
        vm.onAction(RecipeListAction.Search)
        staleGate.complete(Unit)

        assertEquals(success(curry), vm.results())
        assertFalse(vm.uiState.value.isSearching)
    }

    @Test
    fun `params are restored from saved state`() {
        val saved = RecipeSearchParams(query = "curry", vegetarianOnly = true, servingsRange = 6..Int.MAX_VALUE)

        val vm = viewModel(SavedStateHandle(mapOf(SEARCH_PARAMS_KEY to saved.encode())))

        assertEquals(saved, repository.searchCalls.single())
        assertEquals("curry", vm.uiState.value.searchText)
    }

    @Test
    fun `applied params are written to saved state`() {
        val handle = SavedStateHandle()
        val vm = viewModel(handle)

        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true)))

        assertEquals(RecipeSearchParams(vegetarianOnly = true), decodeSearchParams(handle[SEARCH_PARAMS_KEY]!!))
    }

    @Test
    fun `corrupt saved state falls back to defaults`() {
        viewModel(SavedStateHandle(mapOf(SEARCH_PARAMS_KEY to "{ not json")))

        assertEquals(RecipeSearchParams(), repository.searchCalls.single())
    }

    @Test
    fun `active filters are derived from applied params`() {
        val vm = viewModel()

        vm.onAction(RecipeListAction.ApplyFilters(RecipeSearchParams(vegetarianOnly = true, includeIngredients = setOf("rice"))))

        assertEquals(listOf(ActiveFilter.Vegetarian, ActiveFilter.Include("rice")), vm.uiState.value.activeFilters)
    }
}
