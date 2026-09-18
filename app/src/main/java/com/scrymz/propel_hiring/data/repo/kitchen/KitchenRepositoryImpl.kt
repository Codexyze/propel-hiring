package com.scrymz.propel_hiring.data.repo.kitchen

import android.util.Log
import com.scrymz.propel_hiring.data.local.kitchen.KitchenLocalDataSource
import com.scrymz.propel_hiring.data.remote.kitchen.toDomain
import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen
import com.scrymz.propel_hiring.domain.repository.kitchen.KitchenRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class KitchenRepositoryImpl @Inject constructor(
    private val localDataSource: KitchenLocalDataSource
) : KitchenRepository {

    private companion object {
        const val TAG = "KitchenRepositoryImpl"
    }

    override suspend fun getKitchens(): Flow<ResultState<List<Kitchen>>> = flow {
        Log.d(TAG, "getKitchens: Loading kitchens from local JSON asset")
        emit(ResultState.Loading)
        try {
            val dtos = localDataSource.getKitchens()
            val kitchens = dtos.map { it.toDomain() }
            Log.d(TAG, "getKitchens: Loaded ${kitchens.size} kitchens successfully")
            emit(ResultState.Success(kitchens))
        } catch (e: Exception) {
            Log.e(TAG, "getKitchens: Error loading kitchens - ${e.message}", e)
            emit(ResultState.Error(e.message ?: "Failed to load kitchen data"))
        }
    }

    override suspend fun getKitchenById(id: String): Flow<ResultState<Kitchen>> = flow {
        Log.d(TAG, "getKitchenById: Querying kitchen with ID: $id")
        emit(ResultState.Loading)
        try {
            val dtos = localDataSource.getKitchens()
            val kitchen = dtos.find { it.id == id }?.toDomain()
            if (kitchen != null) {
                Log.d(TAG, "getKitchenById: Found kitchen - ${kitchen.name}")
                emit(ResultState.Success(kitchen))
            } else {
                Log.e(TAG, "getKitchenById: Kitchen with ID $id not found")
                emit(ResultState.Error("Kitchen with ID '$id' was not found."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "getKitchenById: Error retrieving kitchen - ${e.message}", e)
            emit(ResultState.Error(e.message ?: "Failed to load kitchen detail"))
        }
    }
}
