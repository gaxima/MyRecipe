# MyRecipe

A native Android app (Kotlin + Jetpack Compose) for browsing and searching cooking recipes. Recipes come from a mock API backed by a bundled JSON file, but the code is structured as if the data came from a real backend.

- **Grid screen:** square recipe cards, a search field, a filter sheet and removable filter chips.
- **Detail screen:** photo, description, dietary attributes, servings, ingredients and numbered instructions.
- **Search endpoint** with optional filters: free text, vegetarian, servings range, include/exclude ingredients, and instruction content.

## Setup

**Requirements**

- Android Studio (recent stable) with Android SDK 37 installed.
- A JDK for Gradle. The Gradle daemon toolchain is JDK 25 (`gradle/gradle-daemon-jvm.properties`). Android Studio's bundled JBR works; from a terminal, point `JAVA_HOME` at it, e.g. `export JAVA_HOME="<Android Studio>/jbr"`.
- Internet access on the device for recipe photos (everything else works offline).

**Build and run**

```bash
./gradlew assembleDebug                 # builds app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug                  # installs on a connected device/emulator
```

Or open the project in Android Studio and run the `app` configuration.

**Tests**

```bash
./gradlew testDebugUnitTest             # JVM unit tests (search rules, mapping, repository, ViewModels)
./gradlew connectedDebugAndroidTest     # Compose UI tests, needs a device or emulator
```

**Seeing the error states.** The fake API can simulate latency and failures. In `app/src/main/java/com/gaxima/myrecipe/data/di/DataModule.kt`, change

```kotlin
fun provideFakeApiConfig(): FakeApiConfig = FakeApiConfig()
```

to e.g. `FakeApiConfig(failureRate = 0.5)` (or `latency = 2.seconds`). A failed first load shows a full-screen error with *Retry*; a failed search with results on screen keeps them and shows a snackbar.

## Architecture

Single `:app` module, layered by package:

```
┌──────────────────────── presentation ────────────────────────┐
│  RecipeListScreen ─► RecipeListViewModel                      │
│  RecipeDetailScreen ─► RecipeDetailViewModel                  │
│  (Compose, Navigation 3, Hilt ViewModels)                     │
└───────────────────────────────┬───────────────────────────────┘
                                │ depends on
┌──────────────────────────── domain ───────────────────────────┐
│  Recipe, Ingredient, DietaryAttribute, RecipeSearchParams      │
│  RecipeRepository (interface), RecipeNotFoundException         │
│  (pure Kotlin – no Android, no framework)                      │
└───────────────────────────────▲───────────────────────────────┘
                                │ implements
┌───────────────────────────── data ────────────────────────────┐
│  RecipeRepositoryImpl ─► RecipeApi (Retrofit-shaped interface) │
│                             └─ FakeRecipeApi ─► RecipeFilter   │
│                                   └─ assets/recipes.json       │
│  DTOs + mapper, Hilt module                                    │
└────────────────────────────────────────────────────────────────┘
```

**Request flow** (e.g. the user types "curry" and presses *Search* on the keyboard):

```
UI ──onAction(Search)──► ViewModel ──searchRecipes(params)──► Repository
                                                                 │  params → endpoint arguments
                                                                 ▼
                                   FakeRecipeApi.searchRecipes(query = "curry", …)
                                     latency / failure simulation → RecipeFilter → DTOs
                                                                 │
UI ◄──StateFlow<UiState>── ViewModel ◄──Result<List<Recipe>>─────┘  (DTO → domain mapping)
UI ◄──SharedFlow<Event>─── (snackbar on a failed refresh)
```

- **State** (ViewModel → UI): one `StateFlow` per screen. The list screen's results are `Loading | Success | Error`, with the search text, applied params and an `isSearching` flag beside them so the search bar and chips stay visible.
- **Events** (ViewModel → UI): a `SharedFlow` used only for snackbars.
- **Actions** (UI → ViewModel): a sealed `RecipeListAction` / `RecipeDetailAction` passed to a single `onAction` function.
- Every screen is a thin `XScreen` (gets the ViewModel and collects state) plus a stateless `XContent`, which is what previews and UI tests use.

## Key design decisions

- **The search runs in the (fake) API, not the UI.** The brief asks for a search *endpoint* with query filters, and real recipe APIs filter on the server because the catalogue is too big to download. `RecipeApi` looks like a Retrofit service; `FakeRecipeApi` is the only class that knows the data is local, and its query logic is a pure `RecipeFilter`. Using a real backend means writing one Retrofit implementation and changing one Hilt binding; `FakeRecipeApi` and `RecipeFilter` are deleted, and `domain` and `presentation` don't change. Filtering a fully loaded list in the ViewModel was the simpler alternative, but it doesn't satisfy the brief and puts query rules in the UI layer.
- **A thin domain without use cases.** Use cases that only forward to the repository add files without adding behaviour. The domain layer still holds the models and the repository interface, which keeps API DTOs out of the UI and makes ViewModels easy to test with a fake repository.
- **DTOs are nullable; the mapper is defensive.** A record without an id, title or positive servings is dropped instead of failing the whole list; a repeated id keeps only its first record (the grid needs unique keys); missing lists become empty and unknown dietary values are ignored so new API values don't break the app.
- **Explicit search instead of search-as-you-type.** The search runs on the keyboard's *Search* action, when the text is cleared, and when filters change. There's no debounce logic, every search is a deliberate request, and the loading indicator matches what the user just did.
- **`SharedFlow` only for events that can be lost.** A `SharedFlow` without replay drops events emitted while nothing collects (e.g. during rotation). So it only carries snackbars; anything that must survive lives in the `StateFlow`.
- **Error policy.** A failed first load replaces the content with a full-screen error and *Retry*. A failed search while results are shown keeps them and shows a snackbar, because replacing a usable list with an error screen is worse.
- **`Result` with cancellation rethrown.** The repository converts exceptions into `Result.failure` but rethrows `CancellationException`, so starting a new search really cancels the old one and a stale response can never overwrite a newer one.
- **Navigation 3 with id-only keys.** The detail key carries only `recipeId`; the detail ViewModel (Hilt assisted injection) loads its own data, which also makes deep links straightforward. The back stack is saved across rotation and process death.
- **Search state survives process death.** The applied search params are saved in `SavedStateHandle`; the filter sheet's draft uses `rememberSaveable`.
- **Testability seams.** `RecipeJsonSource` hides `AssetManager` so the fake server runs on the JVM, `FakeApiConfig` controls latency and failures, and the IO dispatcher is injected.

## Assumptions and tradeoffs

- **The search field matches title and description.** Ingredients and instructions have their own filters, so each control has one meaning (typing "chicken" doesn't return recipes that only mention chicken stock in step 4).
- **All filters combine with AND.** *Must include* needs **all** listed ingredients; *Must not include* removes a recipe if it contains **any** of them.
- **Matching is case-insensitive substring matching**, so "chicken" matches "chicken breast" and excluding "peanut" also excludes "peanut butter" (the safer behaviour for allergies).
- **Vegan counts as vegetarian.** The vegetarian filter matches recipes tagged `vegetarian` or `vegan`.
- **Servings is a range slider from 1 to 8+.** The full range means "any"; the top stop is open-ended.
- **Ingredient quantities are display strings** ("400 g", "a pinch"). They are never used in calculations, so splitting amount and unit wasn't worth it.
- **Adding an ingredient to one list removes it from the other**, so the user can't build a search that can never match.
- **Ingredient fields accept comma-separated lists**: "chicken, rice" adds two chips.
- **Filter changes use the text in the search field**, even if *Search* wasn't pressed, so a typed word is never silently ignored.
- **Images are remote URLs** (TheMealDB) loaded with Coil, with a placeholder while loading and a fallback icon when there is no URL or the load fails. Some photos are the closest available dish rather than the exact recipe.
- **The detail screen loads the recipe again by id** rather than receiving it from the list. That's one extra (fake) call, but it's how the screen would behave with a real API and deep links.

## Known limitations

- No pagination: the endpoint returns every match (fine for 16 recipes, not for a real catalogue).
- No offline data cache or persistence; photos are cached only by Coil's default memory/disk cache.
- Substring matching can over-match (e.g. "egg" also matches "eggplant"); a real backend would use tokenised or full-text search.
- English-only data and UI strings, and no handling of plural ingredient forms ("tomato" vs "tomatoes" happens to work only because of substring matching).
- The fake API reads the JSON once per process; changes to `recipes.json` need an app restart.

## Credits

Recipe photos: [TheMealDB](https://www.themealdb.com/) (free API, used for illustration only). Recipe texts were written for this project.
