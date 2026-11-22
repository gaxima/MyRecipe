package com.gaxima.myrecipe.presentation.list.filter

import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngredientInputTest {

    @Test
    fun `ingredient names are trimmed and lowercased`() {
        assertEquals("chicken", normalizeIngredient("  Chicken "))
    }

    @Test
    fun `blank input is not an ingredient`() {
        assertNull(normalizeIngredient("   "))
    }

    @Test
    fun `comma separated input adds each ingredient`() {
        val params = RecipeSearchParams().withIncluded("Chicken, rice ,, garlic")

        assertEquals(setOf("chicken", "rice", "garlic"), params.includeIngredients)
    }

    @Test
    fun `comma separated exclude moves each ingredient out of include`() {
        val params = RecipeSearchParams(includeIngredients = setOf("rice", "egg")).withExcluded("rice, peanut")

        assertEquals(setOf("egg"), params.includeIngredients)
        assertEquals(setOf("rice", "peanut"), params.excludeIngredients)
    }

    @Test
    fun `adding the same ingredient twice keeps one chip`() {
        val params = RecipeSearchParams().withIncluded("Chicken ").withIncluded("chicken")

        assertEquals(setOf("chicken"), params.includeIngredients)
    }

    @Test
    fun `including an excluded ingredient moves it`() {
        val params = RecipeSearchParams(excludeIngredients = setOf("peanut")).withIncluded("Peanut")

        assertEquals(setOf("peanut"), params.includeIngredients)
        assertEquals(emptySet<String>(), params.excludeIngredients)
    }

    @Test
    fun `excluding an included ingredient moves it`() {
        val params = RecipeSearchParams(includeIngredients = setOf("rice")).withExcluded("rice")

        assertEquals(emptySet<String>(), params.includeIngredients)
        assertEquals(setOf("rice"), params.excludeIngredients)
    }

    @Test
    fun `pending ingredient text is committed on apply`() {
        val params = RecipeSearchParams().withPendingIngredients(include = "Rice ", exclude = "peanut")

        assertEquals(setOf("rice"), params.includeIngredients)
        assertEquals(setOf("peanut"), params.excludeIngredients)
    }

    @Test
    fun `blank pending text commits nothing`() {
        val params = RecipeSearchParams(includeIngredients = setOf("rice"))

        assertEquals(params, params.withPendingIngredients(include = " ", exclude = ""))
    }

    @Test
    fun `blank input leaves params unchanged`() {
        val params = RecipeSearchParams(includeIngredients = setOf("rice"))

        assertEquals(params, params.withIncluded(" "))
        assertEquals(params, params.withExcluded(""))
    }
}
