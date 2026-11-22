package com.gaxima.myrecipe.presentation.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.domain.model.RecipeSearchParams
import com.gaxima.myrecipe.presentation.components.ErrorState
import com.gaxima.myrecipe.presentation.components.LoadingState
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme

/** Stateless recipe list screen: renders [state] and reports user intent through [onAction]. */
@Composable
fun RecipeListContent(
    state: RecipeListUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (RecipeListAction) -> Unit,
    onRecipeClick: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            RecipeSearchBar(
                text = state.searchText,
                activeFilterCount = state.activeFilters.size,
                onTextChange = { onAction(RecipeListAction.SearchTextChange(it)) },
                onSearch = { onAction(RecipeListAction.Search) },
                onClear = { onAction(RecipeListAction.ClearSearch) },
                onFilterClick = onFilterClick,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            )
            if (state.activeFilters.isNotEmpty()) {
                ActiveFilterChips(
                    filters = state.activeFilters,
                    onRemove = { onAction(RecipeListAction.RemoveFilter(it)) },
                )
            }
            // Fixed-height slot so the grid does not jump when the indicator appears.
            Box(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                if (state.isSearching) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            when (val results = state.results) {
                RecipeResultsState.Loading -> LoadingState()
                is RecipeResultsState.Error -> ErrorState(
                    messageRes = results.messageRes,
                    onRetry = { onAction(RecipeListAction.Retry) },
                )
                is RecipeResultsState.Success -> if (results.recipes.isEmpty()) {
                    EmptyResults(onClearFilters = { onAction(RecipeListAction.ClearAllFilters) })
                } else {
                    RecipeGrid(recipes = results.recipes, onRecipeClick = onRecipeClick)
                }
            }
        }
    }
}

@Composable
private fun RecipeGrid(recipes: List<Recipe>, onRecipeClick: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(recipes, key = { it.id }) { recipe ->
            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) })
        }
    }
}

@Composable
private fun EmptyResults(onClearFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(stringResource(R.string.empty_body), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onClearFilters) { Text(stringResource(R.string.clear_search_and_filters)) }
    }
}

@Composable
private fun ListPreview(state: RecipeListUiState) {
    MyRecipeTheme(dynamicColor = false) {
        RecipeListContent(
            state = state,
            snackbarHostState = SnackbarHostState(),
            onAction = {},
            onRecipeClick = {},
            onFilterClick = {},
        )
    }
}

private val previewParams = RecipeSearchParams(
    query = "pasta",
    vegetarianOnly = true,
    servingsRange = 2..4,
    excludeIngredients = setOf("peanut"),
)

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SuccessPreview() = ListPreview(
    RecipeListUiState(results = RecipeResultsState.Success(PreviewData.recipes)),
)

@Preview(showSystemUi = true)
@Composable
private fun SearchingWithFiltersPreview() = ListPreview(
    RecipeListUiState(
        searchText = "pasta",
        appliedParams = previewParams,
        results = RecipeResultsState.Success(PreviewData.recipes.take(2)),
        isSearching = true,
    ),
)

@Preview(showSystemUi = true)
@Composable
private fun LoadingPreview() = ListPreview(RecipeListUiState())

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EmptyPreview() = ListPreview(
    RecipeListUiState(appliedParams = previewParams, results = RecipeResultsState.Success(emptyList())),
)

@Preview(showSystemUi = true)
@Composable
private fun ErrorPreview() = ListPreview(
    RecipeListUiState(results = RecipeResultsState.Error(R.string.error_network)),
)
