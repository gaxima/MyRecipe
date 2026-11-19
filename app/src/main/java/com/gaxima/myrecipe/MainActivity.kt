package com.gaxima.myrecipe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gaxima.myrecipe.navigation.RecipeNavigation
import com.gaxima.myrecipe.presentation.theme.MyRecipeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyRecipeTheme {
                RecipeNavigation()
            }
        }
    }
}
