package com.gaxima.myrecipe.testutil

import com.gaxima.myrecipe.data.remote.dto.IngredientDto
import com.gaxima.myrecipe.data.remote.dto.RecipeDto

fun recipeDto(
    id: String = "r-1",
    title: String = "Title",
    description: String = "",
    servings: Int = 2,
    imageUrl: String? = null,
    dietary: List<String> = emptyList(),
    ingredients: List<Pair<String, String?>> = emptyList(),
    instructions: List<String> = emptyList(),
): RecipeDto = RecipeDto(
    id = id,
    title = title,
    description = description,
    servings = servings,
    imageUrl = imageUrl,
    dietaryAttributes = dietary,
    ingredients = ingredients.map { (name, quantity) -> IngredientDto(name, quantity) },
    instructions = instructions,
)
