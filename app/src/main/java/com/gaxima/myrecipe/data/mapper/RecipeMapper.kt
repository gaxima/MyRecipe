package com.gaxima.myrecipe.data.mapper

import com.gaxima.myrecipe.data.remote.dto.IngredientDto
import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import com.gaxima.myrecipe.domain.model.DietaryAttribute
import com.gaxima.myrecipe.domain.model.Ingredient
import com.gaxima.myrecipe.domain.model.Recipe

/**
 * Maps a DTO to the domain model, or returns `null` when the record is unusable
 * (no id, no title, or no positive serving count). One bad record must not break the list.
 */
fun RecipeDto.toDomainOrNull(): Recipe? {
    val id = id?.takeIf { it.isNotBlank() } ?: return null
    val title = title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val servings = servings?.takeIf { it > 0 } ?: return null

    return Recipe(
        id = id,
        title = title,
        description = description?.trim().orEmpty(),
        servings = servings,
        imageUrl = imageUrl?.trim()?.takeIf { it.isNotEmpty() },
        dietaryAttributes = dietaryAttributes.orEmpty().mapNotNull(::dietaryAttributeOf).toSet(),
        ingredients = ingredients.orEmpty().mapNotNull { it.toDomainOrNull() },
        instructions = instructions.orEmpty().map { it.trim() }.filter { it.isNotEmpty() },
    )
}

fun List<RecipeDto>.toDomain(): List<Recipe> = mapNotNull { it.toDomainOrNull() }

private fun IngredientDto.toDomainOrNull(): Ingredient? {
    val name = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return Ingredient(name = name, quantity = quantity?.trim()?.takeIf { it.isNotEmpty() })
}

/** Unknown values are ignored so new API attributes don't break the app. */
private fun dietaryAttributeOf(value: String): DietaryAttribute? = when (value.trim().lowercase()) {
    "vegetarian" -> DietaryAttribute.VEGETARIAN
    "vegan" -> DietaryAttribute.VEGAN
    "gluten_free" -> DietaryAttribute.GLUTEN_FREE
    "dairy_free" -> DietaryAttribute.DAIRY_FREE
    else -> null
}
