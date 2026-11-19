package com.gaxima.myrecipe.presentation.list

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gaxima.myrecipe.presentation.common.ObserveAsEvents

/** Wires [RecipeListViewModel] to the stateless [RecipeListContent]. */
@Composable
fun RecipeListScreen(
    onRecipeClick: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

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
        onFilterClick = {},
    )
}
