package com.gaxima.myrecipe.presentation.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecipeListContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<RecipeListAction>()
    private val clickedIds = mutableListOf<String>()

    private fun setContent(state: RecipeListUiState) {
        composeRule.setContent {
            MyRecipeTheme(dynamicColor = false) {
                RecipeListContent(
                    state = state,
                    snackbarHostState = SnackbarHostState(),
                    onAction = { actions += it },
                    onRecipeClick = { clickedIds += it },
                    onFilterClick = {},
                )
            }
        }
    }

    private fun successState(params: RecipeSearchParams = RecipeSearchParams()) = RecipeListUiState(
        appliedParams = params,
        results = RecipeResultsState.Success(PreviewData.recipes),
    )

    @Test
    fun successShowsRecipeTitles() {
        setContent(successState())

        composeRule.onNodeWithText(PreviewData.vegetarianRecipe.title).assertExists()
        composeRule.onNodeWithText(PreviewData.recipes[1].title).assertExists()
    }

    @Test
    fun imeSearchSendsSearchAction() {
        setContent(successState())

        composeRule.onNode(hasSetTextAction()).performTextInput("pasta")
        composeRule.onNode(hasSetTextAction()).performImeAction()

        assertTrue(RecipeListAction.SearchTextChange("pasta") in actions)
        assertEquals(RecipeListAction.Search, actions.last())
    }

    @Test
    fun emptyResultsButtonClearsSearchAndFilters() {
        setContent(RecipeListUiState(results = RecipeResultsState.Success(emptyList())))

        composeRule.onNodeWithText("No recipes match your search").assertExists()
        composeRule.onNodeWithText("Clear search and filters").performClick()

        assertEquals(RecipeListAction.ClearAllFilters, actions.last())
    }

    @Test
    fun searchFieldKeepsItsAccessibleNameAfterTyping() {
        setContent(successState().copy(searchText = "pasta"))

        composeRule.onNode(hasSetTextAction() and hasContentDescription("Search recipes")).assertExists()
    }

    @Test
    fun filterButtonAnnouncesHowManyFiltersAreActive() {
        setContent(successState(RecipeSearchParams(vegetarianOnly = true, excludeIngredients = setOf("peanut"))))

        composeRule.onNodeWithContentDescription("Filters, 2 active").assertExists()
    }

    @Test
    fun errorShowsRetryWhichSendsAction() {
        setContent(RecipeListUiState(results = RecipeResultsState.Error(com.gaxima.myrecipe.R.string.error_network)))

        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(RecipeListAction.Retry, actions.last())
    }

    @Test
    fun activeFilterChipRemovalSendsAction() {
        setContent(successState(RecipeSearchParams(vegetarianOnly = true)))

        composeRule.onNodeWithText("Vegetarian").performClick()

        assertEquals(RecipeListAction.RemoveFilter(ActiveFilter.Vegetarian), actions.last())
    }

    @Test
    fun clickingACardReportsItsId() {
        setContent(successState())

        composeRule.onNodeWithText(PreviewData.vegetarianRecipe.title).performClick()

        assertEquals(listOf(PreviewData.vegetarianRecipe.id), clickedIds)
    }

    @Test
    fun cardWithoutImageStillShowsTitle() {
        setContent(RecipeListUiState(results = RecipeResultsState.Success(listOf(PreviewData.noImageRecipe))))

        composeRule.onNodeWithText(PreviewData.noImageRecipe.title).assertExists()
    }

    @Test
    fun longTitleRenders() {
        setContent(RecipeListUiState(results = RecipeResultsState.Success(listOf(PreviewData.longTitleRecipe))))

        composeRule.onNodeWithText(PreviewData.longTitleRecipe.title).assertExists()
    }
}
