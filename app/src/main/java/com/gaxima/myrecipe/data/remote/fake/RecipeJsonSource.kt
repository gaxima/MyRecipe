package com.gaxima.myrecipe.data.remote.fake

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Where the fake server reads its "database" from. Abstracted so [FakeRecipeApi] runs on the JVM in tests. */
fun interface RecipeJsonSource {
    suspend fun read(): String
}

class AssetRecipeJsonSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : RecipeJsonSource {
    override suspend fun read(): String =
        context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }

    private companion object {
        const val FILE_NAME = "recipes.json"
    }
}
