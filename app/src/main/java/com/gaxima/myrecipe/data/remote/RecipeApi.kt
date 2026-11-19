package com.gaxima.myrecipe.data.remote

import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import com.gaxima.myrecipe.data.remote.dto.RecipesResponseDto

/**
 * The recipes backend contract, shaped like a Retrofit service:
 * `GET /recipes/search?q=&vegetarian=&min_servings=&max_servings=&include=&exclude=&instructions=`
 * and `GET /recipes/{id}`. `null` / empty arguments mean "not filtered".
 */
interface RecipeApi {

    suspend fun searchRecipes(
        query: String?,
        vegetarian: Boolean?,
        minServings: Int?,
        maxServings: Int?,
        includeIngredients: List<String>,
        excludeIngredients: List<String>,
        instructionQuery: String?,
    ): RecipesResponseDto

    /** @throws com.gaxima.myrecipe.domain.error.RecipeNotFoundException when no recipe has [id] (HTTP 404 equivalent). */
    suspend fun getRecipe(id: String): RecipeDto
}
