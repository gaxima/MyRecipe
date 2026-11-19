package com.gaxima.myrecipe.data.repository

import com.gaxima.myrecipe.data.remote.RecipeApi
import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import com.gaxima.myrecipe.data.remote.dto.RecipesResponseDto
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.testutil.recipeDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RecipeRepositoryImplTest {

    private data class SearchCall(
        val query: String?,
        val vegetarian: Boolean?,
        val minServings: Int?,
        val maxServings: Int?,
        val include: List<String>,
        val exclude: List<String>,
        val instructionQuery: String?,
    )

    private class RecordingApi : RecipeApi {
        var searchResponse: () -> RecipesResponseDto = { RecipesResponseDto(emptyList()) }
        var detailResponse: () -> RecipeDto = { recipeDto() }
        val calls = mutableListOf<SearchCall>()

        override suspend fun searchRecipes(
            query: String?,
            vegetarian: Boolean?,
            minServings: Int?,
            maxServings: Int?,
            includeIngredients: List<String>,
            excludeIngredients: List<String>,
            instructionQuery: String?,
        ): RecipesResponseDto {
            calls += SearchCall(query, vegetarian, minServings, maxServings, includeIngredients, excludeIngredients, instructionQuery)
            return searchResponse()
        }

        override suspend fun getRecipe(id: String): RecipeDto = detailResponse()
    }

    private val api = RecordingApi()
    private val repository = RecipeRepositoryImpl(api)

    @Test
    fun `params are translated to endpoint arguments`() = runTest {
        repository.searchRecipes(
            RecipeSearchParams(
                query = "curry",
                vegetarianOnly = true,
                servingsRange = 2..4,
                includeIngredients = setOf("rice"),
                excludeIngredients = setOf("peanut"),
                instructionQuery = "simmer",
            ),
        )

        assertEquals(SearchCall("curry", true, 2, 4, listOf("rice"), listOf("peanut"), "simmer"), api.calls.single())
    }

    @Test
    fun `blank strings become null arguments`() = runTest {
        repository.searchRecipes(RecipeSearchParams(query = "  ", instructionQuery = ""))

        assertEquals(SearchCall(null, null, null, null, emptyList(), emptyList(), null), api.calls.single())
    }

    @Test
    fun `open-ended servings range sends no max`() = runTest {
        repository.searchRecipes(RecipeSearchParams(servingsRange = 6..Int.MAX_VALUE))

        assertEquals(6, api.calls.single().minServings)
        assertEquals(null, api.calls.single().maxServings)
    }

    @Test
    fun `successful search maps dtos and drops invalid ones`() = runTest {
        api.searchResponse = { RecipesResponseDto(listOf(recipeDto(id = "ok"), recipeDto(id = "bad", servings = 0))) }

        val result = repository.searchRecipes(RecipeSearchParams())

        assertEquals(listOf("ok"), result.getOrThrow().map { it.id })
    }

    @Test
    fun `api exception becomes failure`() = runTest {
        api.searchResponse = { throw IOException("offline") }

        val result = repository.searchRecipes(RecipeSearchParams())

        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `cancellation is rethrown`() = runTest {
        api.searchResponse = { throw CancellationException("cancelled") }

        val thrown = runCatching { repository.searchRecipes(RecipeSearchParams()) }.exceptionOrNull()

        assertTrue(thrown is CancellationException)
    }

    @Test
    fun `invalid detail dto becomes not found`() = runTest {
        api.detailResponse = { recipeDto(id = "r-9").copy(title = null) }

        val result = repository.getRecipe("r-9")

        assertTrue(result.exceptionOrNull() is RecipeNotFoundException)
    }

    @Test
    fun `detail maps the dto`() = runTest {
        api.detailResponse = { recipeDto(id = "r-9", title = "Soup") }

        assertEquals("Soup", repository.getRecipe("r-9").getOrThrow().title)
    }
}
