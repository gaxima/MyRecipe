package com.gaxima.myrecipe.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R

/**
 * Search field plus filter button. Typing never searches by itself: the search runs on the
 * keyboard's Search action (or when the text is cleared).
 */
@Composable
fun RecipeSearchBar(
    text: String,
    activeFilterCount: Int,
    onTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val hint = stringResource(R.string.search_hint)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            // The placeholder disappears once text is typed; this keeps the field named for TalkBack.
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = hint },
            placeholder = { Text(hint) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear_search))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboard?.hide()
                    onSearch()
                },
            ),
        )
        BadgedBox(
            badge = {
                if (activeFilterCount > 0) {
                    // The count is announced by the button's description instead.
                    Badge(modifier = Modifier.clearAndSetSemantics {}) { Text(activeFilterCount.toString()) }
                }
            },
        ) {
            val description = if (activeFilterCount > 0) {
                pluralStringResource(R.plurals.cd_filters_active, activeFilterCount, activeFilterCount)
            } else {
                stringResource(R.string.cd_filters)
            }
            IconButton(onClick = onFilterClick) {
                Icon(painterResource(R.drawable.ic_filter_list), contentDescription = description)
            }
        }
    }
}
