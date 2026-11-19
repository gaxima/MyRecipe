package com.gaxima.myrecipe.data.repository

import com.gaxima.myrecipe.data.mapper.toDomain
import com.gaxima.myrecipe.data.mapper.toDomainOrNull
import com.gaxima.myrecipe.data.remote.RecipeApi
import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.domain.repository.RecipeRepository
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class RecipeRepositoryImpl @Inject constructor(
    private val api: RecipeApi,
) : RecipeRepository {

    override suspend fun searchRecipes(params: RecipeSearchParams): Result<List<Recipe>> = resultOf {
        api.searchRecipes(
            query = params.query.trim().ifBlank { null },
            vegetarian = params.vegetarianOnly.takeIf { it },
            minServings = params.servingsRange?.first,
            maxServings = params.servingsRange?.last?.takeIf { it != Int.MAX_VALUE },
            includeIngredients = params.includeIngredients.toList(),
            excludeIngredients = params.excludeIngredients.toList(),
            instructionQuery = params.instructionQuery.trim().ifBlank { null },
        ).recipes.orEmpty().toDomain()
    }

    override suspend fun getRecipe(id: String): Result<Recipe> = resultOf {
        api.getRecipe(id).toDomainOrNull() ?: throw RecipeNotFoundException(id)
    }

    /**
     * Like [runCatching], but rethrows [CancellationException] so a cancelled search
     * stops its coroutine instead of being reported as a failure.
     */
    private inline fun <T> resultOf(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}
