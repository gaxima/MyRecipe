package com.gaxima.myrecipe.data.remote.fake

import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import javax.inject.Inject

/**
 * The fake server's query logic. With a real backend this runs server-side and is deleted
 * together with [FakeRecipeApi].
 *
 * Every criterion is optional and they combine with AND. Text comparisons are trimmed,
 * case-insensitive substring matches; blank criteria are ignored.
 */
class RecipeFilter @Inject constructor() {

    fun filter(
        recipes: List<RecipeDto>,
        query: String?,
        vegetarian: Boolean?,
        minServings: Int?,
        maxServings: Int?,
        includeIngredients: List<String>,
        excludeIngredients: List<String>,
        instructionQuery: String?,
    ): List<RecipeDto> {
        val text = query.normalized()
        val instructionText = instructionQuery.normalized()
        val include = includeIngredients.mapNotNull { it.normalized() }
        val exclude = excludeIngredients.mapNotNull { it.normalized() }

        return recipes.filter { recipe ->
            recipe.matchesText(text) &&
                (vegetarian != true || recipe.isVegetarian()) &&
                recipe.matchesServings(minServings, maxServings) &&
                include.all { recipe.hasIngredient(it) } &&
                exclude.none { recipe.hasIngredient(it) } &&
                recipe.instructionsContain(instructionText)
        }
    }

    private fun String?.normalized(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

    private fun RecipeDto.matchesText(text: String?): Boolean =
        text == null ||
            title.orEmpty().contains(text, ignoreCase = true) ||
            description.orEmpty().contains(text, ignoreCase = true)

    private fun RecipeDto.isVegetarian(): Boolean =
        dietaryAttributes.orEmpty().any {
            it.equals(VEGETARIAN, ignoreCase = true) || it.equals(VEGAN, ignoreCase = true)
        }

    private fun RecipeDto.matchesServings(min: Int?, max: Int?): Boolean {
        val count = servings ?: return min == null && max == null
        return (min == null || count >= min) && (max == null || count <= max)
    }

    private fun RecipeDto.hasIngredient(name: String): Boolean =
        ingredients.orEmpty().any { it.name.orEmpty().contains(name, ignoreCase = true) }

    private fun RecipeDto.instructionsContain(text: String?): Boolean =
        text == null || instructions.orEmpty().any { it.contains(text, ignoreCase = true) }

    private companion object {
        const val VEGETARIAN = "vegetarian"
        const val VEGAN = "vegan"
    }
}
