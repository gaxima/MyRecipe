package com.gaxima.myrecipe.data

import com.gaxima.myrecipe.data.mapper.toDomain
import com.gaxima.myrecipe.data.remote.dto.RecipeDto
import com.gaxima.myrecipe.data.remote.dto.RecipesResponseDto
import com.gaxima.myrecipe.data.remote.fake.RecipeFilter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Guards the mock data we ship: a typo in recipes.json would otherwise only show up at runtime. */
class BundledRecipesJsonTest {

    private val dtos: List<RecipeDto> = Json { ignoreUnknownKeys = true }
        .decodeFromString<RecipesResponseDto>(File("src/main/assets/recipes.json").readText())
        .recipes.orEmpty()

    private val filter = RecipeFilter()

    private fun count(
        vegetarian: Boolean? = null,
        minServings: Int? = null,
        maxServings: Int? = null,
        include: List<String> = emptyList(),
        exclude: List<String> = emptyList(),
        instructionQuery: String? = null,
    ) = filter.filter(dtos, null, vegetarian, minServings, maxServings, include, exclude, instructionQuery).size

    @Test
    fun `bundled json parses and every record is valid`() {
        assertTrue("expected at least 15 recipes, got ${dtos.size}", dtos.size >= 15)
        assertEquals(dtos.size, dtos.toDomain().size)
    }

    @Test
    fun `recipe ids are unique`() {
        assertEquals(dtos.size, dtos.map { it.id }.toSet().size)
    }

    @Test
    fun `every filter has matches in the bundled data`() {
        val total = dtos.size
        assertTrue(count(vegetarian = true) in 1 until total)
        assertTrue(count(include = listOf("chicken")) >= 2)
        assertTrue(count(exclude = listOf("peanut")) < total)
        assertTrue(count(instructionQuery = "oven") >= 2)
        assertTrue(count(minServings = 1, maxServings = 1) >= 1)
        assertTrue(count(minServings = 8) >= 1)
    }

    @Test
    fun `every recipe has an https image url`() {
        dtos.forEach { assertTrue("${it.id} image: ${it.imageUrl}", it.imageUrl.orEmpty().startsWith("https://")) }
    }
}
