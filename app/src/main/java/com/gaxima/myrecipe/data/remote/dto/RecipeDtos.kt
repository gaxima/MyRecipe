package com.gaxima.myrecipe.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Wire format of the recipes API. Every field is nullable: a remote payload is never trusted. */
@Serializable
data class RecipesResponseDto(
    val recipes: List<RecipeDto>? = null,
)

@Serializable
data class RecipeDto(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val servings: Int? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("dietary_attributes") val dietaryAttributes: List<String>? = null,
    val ingredients: List<IngredientDto>? = null,
    val instructions: List<String>? = null,
)

@Serializable
data class IngredientDto(
    val name: String? = null,
    val quantity: String? = null,
)
