package com.gaxima.myrecipe.domain.repository

import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams

interface RecipeRepository {
    suspend fun searchRecipes(params: RecipeSearchParams): Result<List<Recipe>>

    /** Fails with [com.gaxima.myrecipe.domain.error.RecipeNotFoundException] for unknown ids. */
    suspend fun getRecipe(id: String): Result<Recipe>
}
