package com.gaxima.myrecipe.testutil

import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.domain.repository.RecipeRepository
import kotlinx.coroutines.CompletableDeferred

class FakeRecipeRepository : RecipeRepository {

    var searchResult: Result<List<Recipe>> = Result.success(emptyList())
    var recipeResult: Result<Recipe> = Result.failure(RecipeNotFoundException("unset"))

    /** When set, calls suspend until it completes. Results are captured before suspending. */
    var searchGate: CompletableDeferred<Unit>? = null
    var recipeGate: CompletableDeferred<Unit>? = null

    val searchCalls = mutableListOf<RecipeSearchParams>()
    val recipeCalls = mutableListOf<String>()

    override suspend fun searchRecipes(params: RecipeSearchParams): Result<List<Recipe>> {
        searchCalls += params
        val result = searchResult
        searchGate?.await()
        return result
    }

    override suspend fun getRecipe(id: String): Result<Recipe> {
        recipeCalls += id
        val result = recipeResult
        recipeGate?.await()
        return result
    }
}
