package com.gaxima.myrecipe.data.remote.fake

import com.gaxima.myrecipe.testutil.recipeDto
import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeFilterTest {

    private val curry = recipeDto(
        id = "curry",
        title = "Chicken Curry",
        description = "Rich and creamy",
        servings = 4,
        ingredients = listOf("chicken" to "500 g", "rice" to "1 cup", "coconut milk" to "400 ml"),
        instructions = listOf("Simmer the chicken in the sauce."),
    )
    private val stirFry = recipeDto(
        id = "stir-fry",
        title = "Veggie Stir-fry",
        servings = 2,
        dietary = listOf("vegan"),
        ingredients = listOf("tofu" to null, "broccoli" to null, "soy sauce" to null, "rice" to null),
        instructions = listOf("Stir-fry in a hot wok."),
    )
    private val noodles = recipeDto(
        id = "noodles",
        title = "Peanut Noodles",
        servings = 2,
        dietary = listOf("vegan"),
        ingredients = listOf("noodles" to null, "peanut butter" to null, "soy sauce" to null),
        instructions = listOf("Boil the noodles, toss with sauce."),
    )
    private val roast = recipeDto(
        id = "roast",
        title = "Roast Chicken",
        description = "Sunday classic",
        servings = 6,
        ingredients = listOf("chicken breast" to null, "potatoes" to null),
        instructions = listOf("Roast in the oven for 1 hour."),
    )
    private val all = listOf(curry, stirFry, noodles, roast)

    private val filter = RecipeFilter()

    private fun search(
        query: String? = null,
        vegetarian: Boolean? = null,
        minServings: Int? = null,
        maxServings: Int? = null,
        include: List<String> = emptyList(),
        exclude: List<String> = emptyList(),
        instructionQuery: String? = null,
    ): List<String> = filter.filter(
        recipes = all,
        query = query,
        vegetarian = vegetarian,
        minServings = minServings,
        maxServings = maxServings,
        includeIngredients = include,
        excludeIngredients = exclude,
        instructionQuery = instructionQuery,
    ).map { it.id!! }

    private val allIds = listOf("curry", "stir-fry", "noodles", "roast")

    @Test
    fun `no criteria returns all recipes`() {
        assertEquals(allIds, search())
    }

    @Test
    fun `query matches title case-insensitively`() {
        assertEquals(listOf("curry"), search(query = "CURRY"))
    }

    @Test
    fun `query matches description`() {
        assertEquals(listOf("roast"), search(query = "sunday"))
    }

    @Test
    fun `query does not match ingredients or instructions`() {
        assertEquals(emptyList<String>(), search(query = "tofu"))
        assertEquals(emptyList<String>(), search(query = "wok"))
    }

    @Test
    fun `blank and padded text is trimmed`() {
        assertEquals(listOf("curry"), search(query = "  curry "))
        assertEquals(allIds, search(query = "   "))
        assertEquals(allIds, search(instructionQuery = "  "))
    }

    @Test
    fun `vegetarian includes vegan recipes`() {
        assertEquals(listOf("stir-fry", "noodles"), search(vegetarian = true))
        assertEquals(allIds, search(vegetarian = false))
        assertEquals(allIds, search(vegetarian = null))
    }

    @Test
    fun `servings bounds are inclusive`() {
        assertEquals(listOf("curry", "stir-fry", "noodles"), search(minServings = 2, maxServings = 4))
        assertEquals(listOf("roast"), search(minServings = 6))
        assertEquals(listOf("stir-fry", "noodles"), search(maxServings = 2))
    }

    @Test
    fun `include requires all ingredients`() {
        assertEquals(listOf("curry"), search(include = listOf("chicken", "rice")))
    }

    @Test
    fun `include matches partial names`() {
        assertEquals(listOf("curry", "roast"), search(include = listOf("chicken")))
    }

    @Test
    fun `exclude removes recipes containing any`() {
        assertEquals(listOf("stir-fry", "roast"), search(exclude = listOf("peanut", "coconut")))
    }

    @Test
    fun `instruction query searches steps`() {
        assertEquals(listOf("roast"), search(instructionQuery = "oven"))
        assertEquals(listOf("stir-fry"), search(instructionQuery = "WOK"))
    }

    @Test
    fun `criteria combine with AND`() {
        assertEquals(listOf("curry"), search(query = "chicken", include = listOf("rice")))
        assertEquals(listOf("stir-fry"), search(vegetarian = true, exclude = listOf("peanut")))
    }

    @Test
    fun `ingredient criteria ignore blank entries`() {
        assertEquals(allIds, search(include = listOf(" ")))
        assertEquals(allIds, search(exclude = listOf("")))
    }
}
