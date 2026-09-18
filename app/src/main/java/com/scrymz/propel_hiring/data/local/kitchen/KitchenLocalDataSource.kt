package com.scrymz.propel_hiring.data.local.kitchen

import android.content.Context
import com.scrymz.propel_hiring.data.remote.kitchen.KitchenDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KitchenLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun getKitchens(): List<KitchenDto> = withContext(ioDispatcher) {
        val jsonString = context.assets.open("kitchens.json").bufferedReader().use { it.readText() }
        json.decodeFromString<List<KitchenDto>>(jsonString)
    }
}
