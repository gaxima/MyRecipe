package com.gaxima.myrecipe.domain.model

data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val servings: Int,
    val imageUrl: String?,
    val dietaryAttributes: Set<DietaryAttribute>,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
) {
    /** Every vegan dish is vegetarian, so either attribute qualifies. */
    val isVegetarian: Boolean
        get() = DietaryAttribute.VEGETARIAN in dietaryAttributes || DietaryAttribute.VEGAN in dietaryAttributes
}

data class Ingredient(
    val name: String,
    /** Free-form display text such as "400 g" or "a pinch"; never used for arithmetic. */
    val quantity: String?,
)

enum class DietaryAttribute { VEGETARIAN, VEGAN, GLUTEN_FREE, DAIRY_FREE }
