package com.gaxima.myrecipe.presentation.list

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gaxima.myrecipe.R
import com.gaxima.myrecipe.domain.model.Recipe
import com.gaxima.myrecipe.presentation.components.RecipeImage
import com.gaxima.myrecipe.presentation.preview.PreviewData
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme

/** Square grid tile: photo with the title and quick facts over a dark gradient for legibility. */
@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.aspectRatio(1f)) {
        Box(modifier = Modifier.fillMaxSize()) {
            RecipeImage(
                imageUrl = recipe.imageUrl,
                contentDescription = null, // decorative: the title is right below
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                    .padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = recipe.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (recipe.isVegetarian) {
                        Icon(
                            painter = painterResource(R.drawable.ic_eco),
                            contentDescription = stringResource(R.string.dietary_vegetarian),
                            tint = Color(0xFFA1D39A),
                            modifier = Modifier.size(16.dp),
                        )
                        Box(Modifier.width(6.dp))
                    }
                    Text(
                        text = pluralStringResource(R.plurals.servings_count, recipe.servings, recipe.servings),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 180)
@Preview(widthDp = 180, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecipeCardPreview() {
    MyRecipeTheme(dynamicColor = false) {
        RecipeCard(recipe = PreviewData.vegetarianRecipe, onClick = {})
    }
}

@Preview(widthDp = 180)
@Composable
private fun RecipeCardLongTitlePreview() {
    MyRecipeTheme(dynamicColor = false) {
        RecipeCard(recipe = PreviewData.longTitleRecipe, onClick = {})
    }
}

@Preview(widthDp = 180)
@Composable
private fun RecipeCardNoImagePreview() {
    MyRecipeTheme(dynamicColor = false) {
        RecipeCard(recipe = PreviewData.noImageRecipe, onClick = {})
    }
}
