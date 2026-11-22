package com.gaxima.myrecipe.data.mapper

import com.gaxima.myrecipe.data.remote.dto.IngredientDto
import com.gaxima.myrecipe.domain.model.DietaryAttribute.GLUTEN_FREE
import com.gaxima.myrecipe.domain.model.DietaryAttribute.VEGAN
import com.gaxima.myrecipe.domain.model.DietaryAttribute.VEGETARIAN
import com.gaxima.myrecipe.domain.model.Ingredient
import com.gaxima.myrecipe.testutil.recipeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeMapperTest {

    @Test
    fun `valid dto maps all fields`() {
        val dto = recipeDto(
            id = "r-1",
            title = "Pasta",
            description = "Quick",
            servings = 4,
            imageUrl = "https://example.com/pasta.jpg",
            dietary = listOf("vegetarian", "gluten_free"),
            ingredients = listOf("spaghetti" to "400 g", "salt" to null),
            instructions = listOf("Boil", "Serve"),
        )

        val recipe = dto.toDomainOrNull()!!

        assertEquals("r-1", recipe.id)
        assertEquals("Pasta", recipe.title)
        assertEquals("Quick", recipe.description)
        assertEquals(4, recipe.servings)
        assertEquals("https://example.com/pasta.jpg", recipe.imageUrl)
        assertEquals(setOf(VEGETARIAN, GLUTEN_FREE), recipe.dietaryAttributes)
        assertEquals(listOf(Ingredient("spaghetti", "400 g"), Ingredient("salt", null)), recipe.ingredients)
        assertEquals(listOf("Boil", "Serve"), recipe.instructions)
    }

    @Test
    fun `missing id, blank title or non-positive servings returns null`() {
        assertNull(recipeDto().copy(id = null).toDomainOrNull())
        assertNull(recipeDto(title = "  ").toDomainOrNull())
        assertNull(recipeDto(servings = 0).toDomainOrNull())
        assertNull(recipeDto().copy(servings = null).toDomainOrNull())
    }

    @Test
    fun `missing optional fields get defaults`() {
        val dto = recipeDto().copy(
            description = null,
            ingredients = null,
            instructions = null,
            dietaryAttributes = null,
        )

        val recipe = dto.toDomainOrNull()!!

        assertEquals("", recipe.description)
        assertTrue(recipe.ingredients.isEmpty())
        assertTrue(recipe.instructions.isEmpty())
        assertTrue(recipe.dietaryAttributes.isEmpty())
    }

    @Test
    fun `unknown dietary values are ignored`() {
        val recipe = recipeDto(dietary = listOf("keto", "vegan")).toDomainOrNull()!!

        assertEquals(setOf(VEGAN), recipe.dietaryAttributes)
    }

    @Test
    fun `blank instruction steps and nameless ingredients are dropped`() {
        val dto = recipeDto(instructions = listOf("Boil", "  ", "Serve")).copy(
            ingredients = listOf(IngredientDto("rice", "1 cup"), IngredientDto(null, "2"), IngredientDto(" ", null)),
        )

        val recipe = dto.toDomainOrNull()!!

        assertEquals(listOf("Boil", "Serve"), recipe.instructions)
        assertEquals(listOf(Ingredient("rice", "1 cup")), recipe.ingredients)
    }

    @Test
    fun `blank image url becomes null`() {
        assertNull(recipeDto(imageUrl = "  ").toDomainOrNull()!!.imageUrl)
    }

    @Test
    fun `list mapping skips invalid records`() {
        val dtos = listOf(recipeDto(id = "a"), recipeDto(id = "b", servings = -1), recipeDto(id = "c"))

        assertEquals(listOf("a", "c"), dtos.toDomain().map { it.id })
    }

    @Test
    fun `list mapping keeps only the first record of a duplicated id`() {
        val dtos = listOf(recipeDto(id = "a", title = "First"), recipeDto(id = "b"), recipeDto(id = "a", title = "Second"))

        val recipes = dtos.toDomain()

        assertEquals(listOf("a", "b"), recipes.map { it.id })
        assertEquals("First", recipes.first().title)
    }

    @Test
    fun `vegan recipe counts as vegetarian`() {
        assertTrue(recipeDto(dietary = listOf("vegan")).toDomainOrNull()!!.isVegetarian)
        assertFalse(recipeDto(dietary = emptyList()).toDomainOrNull()!!.isVegetarian)
        assertNotNull(recipeDto(dietary = listOf("vegetarian")).toDomainOrNull()?.takeIf { it.isVegetarian })
    }
}
