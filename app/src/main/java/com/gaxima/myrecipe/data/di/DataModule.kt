package com.gaxima.myrecipe.data.di

import com.gaxima.myrecipe.data.remote.RecipeApi
import com.gaxima.myrecipe.data.remote.fake.AssetRecipeJsonSource
import com.gaxima.myrecipe.data.remote.fake.FakeApiConfig
import com.gaxima.myrecipe.data.remote.fake.FakeRecipeApi
import com.gaxima.myrecipe.data.remote.fake.RecipeJsonSource
import com.gaxima.myrecipe.data.repository.RecipeRepositoryImpl
import com.gaxima.myrecipe.domain.repository.RecipeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindRecipeRepository(impl: RecipeRepositoryImpl): RecipeRepository

    /** Swap this binding for a Retrofit implementation to talk to a real backend. */
    @Binds
    abstract fun bindRecipeApi(impl: FakeRecipeApi): RecipeApi

    @Binds
    abstract fun bindRecipeJsonSource(impl: AssetRecipeJsonSource): RecipeJsonSource

    companion object {

        @Provides
        @Singleton
        fun provideJson(): Json = Json { ignoreUnknownKeys = true }

        /** Raise `failureRate` (e.g. to 0.3) to see the app's error states. */
        @Provides
        fun provideFakeApiConfig(): FakeApiConfig = FakeApiConfig()

        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
