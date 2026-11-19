package com.gaxima.myrecipe.presentation.list

import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import org.junit.Assert.assertEquals
import org.junit.Test

class ActiveFilterTest {

    private val everything = RecipeSearchParams(
        query = "curry",
        vegetarianOnly = true,
        servingsRange = 2..4,
        includeIngredients = setOf("rice", "chicken"),
        excludeIngredients = setOf("peanut"),
        instructionQuery = "simmer",
    )

    @Test
    fun `active filters are listed in a stable order`() {
        assertEquals(
            listOf(
                ActiveFilter.Vegetarian,
                ActiveFilter.Servings(2..4),
                ActiveFilter.Include("rice"),
                ActiveFilter.Include("chicken"),
                ActiveFilter.Exclude("peanut"),
                ActiveFilter.Instructions("simmer"),
            ),
            everything.toActiveFilters(),
        )
    }

    @Test
    fun `default params have no active filters`() {
        assertEquals(emptyList<ActiveFilter>(), RecipeSearchParams(query = "pasta").toActiveFilters())
    }

    @Test
    fun `blank instruction query is not an active filter`() {
        assertEquals(emptyList<ActiveFilter>(), RecipeSearchParams(instructionQuery = "  ").toActiveFilters())
    }

    @Test
    fun `removing an include filter removes only that ingredient`() {
        val result = everything.without(ActiveFilter.Include("rice"))

        assertEquals(setOf("chicken"), result.includeIngredients)
        assertEquals(everything.copy(includeIngredients = setOf("chicken")), result)
    }

    @Test
    fun `removing each kind of filter clears it`() {
        assertEquals(false, everything.without(ActiveFilter.Vegetarian).vegetarianOnly)
        assertEquals(null, everything.without(ActiveFilter.Servings(2..4)).servingsRange)
        assertEquals(emptySet<String>(), everything.without(ActiveFilter.Exclude("peanut")).excludeIngredients)
        assertEquals("", everything.without(ActiveFilter.Instructions("simmer")).instructionQuery)
    }

    @Test
    fun `clearing filters keeps the query`() {
        assertEquals(RecipeSearchParams(query = "curry"), everything.clearedFilters())
    }

    @Test
    fun `search params survive an encode and decode round trip`() {
        val openEnded = everything.copy(servingsRange = 6..Int.MAX_VALUE)

        assertEquals(everything, decodeSearchParams(everything.encode()))
        assertEquals(openEnded, decodeSearchParams(openEnded.encode()))
        assertEquals(RecipeSearchParams(), decodeSearchParams(RecipeSearchParams().encode()))
    }
}
