package com.gaxima.myrecipe.domain.error

class RecipeNotFoundException(val recipeId: String) : Exception("Recipe $recipeId not found")
