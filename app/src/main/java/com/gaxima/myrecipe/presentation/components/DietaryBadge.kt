package com.gaxima.myrecipe.presentation.components

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.DietaryAttribute
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme

@StringRes
fun DietaryAttribute.labelRes(): Int = when (this) {
    DietaryAttribute.VEGETARIAN -> R.string.dietary_vegetarian
    DietaryAttribute.VEGAN -> R.string.dietary_vegan
    DietaryAttribute.GLUTEN_FREE -> R.string.dietary_gluten_free
    DietaryAttribute.DAIRY_FREE -> R.string.dietary_dairy_free
}

/** A small pill naming a dietary attribute; plant-based ones get a leaf. */
@Composable
fun DietaryBadge(attribute: DietaryAttribute, modifier: Modifier = Modifier) {
    val plantBased = attribute == DietaryAttribute.VEGETARIAN || attribute == DietaryAttribute.VEGAN
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = if (plantBased) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (plantBased) {
                Icon(
                    painter = painterResource(R.drawable.ic_eco),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(text = stringResource(attribute.labelRes()), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DietaryBadgePreview() {
    MyRecipeTheme(dynamicColor = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DietaryAttribute.entries.forEach { DietaryBadge(it) }
        }
    }
}
