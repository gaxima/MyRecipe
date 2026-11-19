package com.gaxima.myrecipe.data.remote.fake

import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Network simulation for [FakeRecipeApi].
 *
 * @property latency delay applied to every call so loading states are visible.
 * @property failureRate probability in `0.0..1.0` that a call fails with an `IOException`.
 */
data class FakeApiConfig(
    val latency: Duration = 500.milliseconds,
    val failureRate: Double = 0.0,
    val random: Random = Random.Default,
)
