package com.gaxima.myrecipe.presentation.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaxima.myrecipe.presentation.common.ObserveAsEvents
import com.gaxima.myrecipe.presentation.list.filter.FilterSheet

/** Wires [RecipeListViewModel] to the stateless [RecipeListContent] and hosts the filter sheet. */
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    // Sheet visibility is pure UI state, so it lives here rather than in the ViewModel.
    var showFilters by rememberSaveable { mutableStateOf(false) }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RecipeListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(resources.getString(event.messageRes))
        }
    }

    RecipeListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onRecipeClick = onRecipeClick,
        onFilterClick = { showFilters = true },
    )

    if (showFilters) {
        FilterSheet(
            initialParams = state.appliedParams,
            onApply = {
                viewModel.onAction(RecipeListAction.ApplyFilters(it))
                showFilters = false
            },
            onDismiss = { showFilters = false },
        )
    }
}
