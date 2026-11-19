package com.gaxima.myrecipe.data.remote.fake

import com.gaxima.myrecipe.data.di.IoDispatcher
import com.gaxima.myrecipe.data.remote.RecipeApi
import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import com.gaxima.myrecipe.data.remote.dto.RecipesResponseDto
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stands in for a remote server: the only class that knows the data is a bundled JSON file.
 * Swapping in a real backend means replacing this binding with a Retrofit implementation of [RecipeApi].
 */
@Singleton
class FakeRecipeApi @Inject constructor(
    private val jsonSource: RecipeJsonSource,
    private val filter: RecipeFilter,
    private val config: FakeApiConfig,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : RecipeApi {

    private val mutex = Mutex()
    private var cache: List<RecipeDto>? = null

    override suspend fun searchRecipes(
        query: String?,
        vegetarian: Boolean?,
        minServings: Int?,
        maxServings: Int?,
        includeIngredients: List<String>,
        excludeIngredients: List<String>,
        instructionQuery: String?,
    ): RecipesResponseDto {
        simulateNetwork()
        val matches = filter.filter(
            recipes = allRecipes(),
            query = query,
            vegetarian = vegetarian,
            minServings = minServings,
            maxServings = maxServings,
            includeIngredients = includeIngredients,
            excludeIngredients = excludeIngredients,
            instructionQuery = instructionQuery,
        )
        return RecipesResponseDto(recipes = matches)
    }

    override suspend fun getRecipe(id: String): RecipeDto {
        simulateNetwork()
        return allRecipes().firstOrNull { it.id == id } ?: throw RecipeNotFoundException(id)
    }

    private suspend fun simulateNetwork() {
        delay(config.latency)
        if (config.random.nextDouble() < config.failureRate) {
            throw IOException("Simulated network failure")
        }
    }

    /** Parses the JSON once; a failed parse is not cached, so a later call retries. */
    private suspend fun allRecipes(): List<RecipeDto> = mutex.withLock {
        cache ?: withContext(ioDispatcher) {
            json.decodeFromString<RecipesResponseDto>(jsonSource.read()).recipes.orEmpty()
        }.also { cache = it }
    }
}
