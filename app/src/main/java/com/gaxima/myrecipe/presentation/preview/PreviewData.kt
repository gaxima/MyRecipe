package com.gaxima.myrecipe.presentation.preview

import com.gaxima.myrecipe.domain.model.DietaryAttribute
import com.gaxima.myrecipe.domain.model.Ingredient
import com.gaxima.myrecipe.domain.model.Recipe

/** Sample data for `@Preview`s and UI tests. */
object PreviewData {

    val vegetarianRecipe = Recipe(
        id = "p-1",
        title = "Creamy Garlic Pasta",
        description = "A quick weeknight pasta tossed in a silky garlic and parmesan sauce.",
        servings = 4,
        imageUrl = "https://www.themealdb.com/images/media/meals/llcbn01574260722.jpg",
        dietaryAttributes = setOf(DietaryAttribute.VEGETARIAN),
        ingredients = listOf(
            Ingredient("spaghetti", "400 g"),
            Ingredient("garlic", "6 cloves"),
            Ingredient("parmesan", "60 g"),
            Ingredient("black pepper", null),
        ),
        instructions = listOf(
            "Boil the spaghetti in well-salted water until al dente.",
            "Gently fry the sliced garlic in butter until fragrant.",
            "Toss the pasta in the sauce and serve with parmesan.",
        ),
    )

    val longTitleRecipe = Recipe(
        id = "p-2",
        title = "Slow-Roasted Pork Shoulder with Crackling, Apple Sauce and Roast Potatoes",
        description = "A long description that keeps going to check how wrapping behaves on narrow screens " +
            "and whether the layout stays readable when the text is much longer than usual.",
        servings = 8,
        imageUrl = "https://www.themealdb.com/images/media/meals/nlxald1764112200.jpg",
        dietaryAttributes = setOf(DietaryAttribute.GLUTEN_FREE, DietaryAttribute.DAIRY_FREE),
        ingredients = listOf(Ingredient("pork shoulder", "2 kg"), Ingredient("apples", "3")),
        instructions = listOf("Roast in the oven for 4 hours."),
    )

    val noImageRecipe = Recipe(
        id = "p-3",
        title = "Avocado Toast",
        description = "Smashed avocado on crunchy sourdough.",
        servings = 1,
        imageUrl = null,
        dietaryAttributes = setOf(DietaryAttribute.VEGAN),
        ingredients = listOf(Ingredient("sourdough bread", "2 slices"), Ingredient("avocado", "1")),
        instructions = listOf("Toast the bread.", "Mash and spread the avocado."),
    )

    val recipes: List<Recipe> = listOf(
        vegetarianRecipe,
        Recipe(
            id = "p-4",
            title = "Chicken Curry",
            description = "A fragrant coconut curry.",
            servings = 4,
            imageUrl = "https://www.themealdb.com/images/media/meals/vwrpps1503068729.jpg",
            dietaryAttributes = setOf(DietaryAttribute.GLUTEN_FREE),
            ingredients = listOf(Ingredient("chicken thighs", "600 g"), Ingredient("coconut milk", "400 ml")),
            instructions = listOf("Simmer the chicken in the sauce."),
        ),
        longTitleRecipe,
        noImageRecipe,
        Recipe(
            id = "p-5",
            title = "Shakshuka",
            description = "Eggs poached in spiced tomato sauce.",
            servings = 3,
            imageUrl = "https://www.themealdb.com/images/media/meals/g373701551450225.jpg",
            dietaryAttributes = setOf(DietaryAttribute.VEGETARIAN, DietaryAttribute.GLUTEN_FREE),
            ingredients = listOf(Ingredient("eggs", "6")),
            instructions = listOf("Simmer the sauce.", "Poach the eggs."),
        ),
        Recipe(
            id = "p-6",
            title = "Beef Tacos",
            description = "Spiced beef in warm tortillas.",
            servings = 4,
            imageUrl = "https://www.themealdb.com/images/media/meals/ypxvwv1505333929.jpg",
            dietaryAttributes = emptySet(),
            ingredients = listOf(Ingredient("beef mince", "500 g")),
            instructions = listOf("Brown the beef.", "Fill the tortillas."),
        ),
    )
}
