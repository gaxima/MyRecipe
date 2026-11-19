package com.gaxima.myrecipe.data.remote.fake

import com.gaxima.myrecipe.domain.error.RecipeNotFoundException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class FakeRecipeApiTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val payload = """
        {
          "recipes": [
            { "id": "curry", "title": "Chicken Curry", "servings": 4,
              "ingredients": [ { "name": "chicken", "quantity": "500 g" } ],
              "instructions": [ "Simmer." ] },
            { "id": "salad", "title": "Greek Salad", "servings": 2, "dietary_attributes": ["vegetarian"] }
          ]
        }
    """.trimIndent()

    private class CountingSource(private val content: String) : RecipeJsonSource {
        var reads = 0
        override suspend fun read(): String {
            reads++
            return content
        }
    }

    private fun TestScope.api(
        source: RecipeJsonSource = CountingSource(payload),
        config: FakeApiConfig = FakeApiConfig(latency = Duration.ZERO),
    ) = FakeRecipeApi(
        jsonSource = source,
        filter = RecipeFilter(),
        config = config,
        json = json,
        ioDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

    private suspend fun FakeRecipeApi.search(query: String? = null) = searchRecipes(
        query = query,
        vegetarian = null,
        minServings = null,
        maxServings = null,
        includeIngredients = emptyList(),
        excludeIngredients = emptyList(),
        instructionQuery = null,
    )

    @Test
    fun `search returns filtered recipes`() = runTest {
        val response = api().search(query = "curry")

        assertEquals(listOf("curry"), response.recipes.orEmpty().map { it.id })
    }

    @Test
    fun `getRecipe returns matching dto`() = runTest {
        assertEquals("Greek Salad", api().getRecipe("salad").title)
    }

    @Test
    fun `getRecipe with unknown id throws RecipeNotFoundException`() = runTest {
        val error = runCatching { api().getRecipe("missing") }.exceptionOrNull()

        assertTrue(error is RecipeNotFoundException)
    }

    @Test
    fun `failure rate of one throws IOException`() = runTest {
        val failing = api(config = FakeApiConfig(latency = Duration.ZERO, failureRate = 1.0))

        assertTrue(runCatching { failing.search() }.exceptionOrNull() is IOException)
        assertTrue(runCatching { failing.getRecipe("curry") }.exceptionOrNull() is IOException)
    }

    @Test
    fun `json is read only once across calls`() = runTest {
        val source = CountingSource(payload)
        val api = api(source = source)

        api.search()
        api.search(query = "salad")
        api.getRecipe("curry")

        assertEquals(1, source.reads)
    }

    @Test
    fun `malformed json surfaces SerializationException`() = runTest {
        val error = runCatching { api(source = CountingSource("{ not json")).search() }.exceptionOrNull()

        assertTrue(error is SerializationException)
    }

    @Test
    fun `latency is applied`() = runTest {
        val api = FakeRecipeApi(
            jsonSource = CountingSource(payload),
            filter = RecipeFilter(),
            config = FakeApiConfig(latency = 500.milliseconds),
            json = json,
            ioDispatcher = StandardTestDispatcher(testScheduler),
        )

        val call = async { api.search() }
        advanceTimeBy(499)
        runCurrent()
        assertFalse(call.isCompleted)

        advanceTimeBy(2)
        runCurrent()
        assertTrue(call.isCompleted)
    }
}
