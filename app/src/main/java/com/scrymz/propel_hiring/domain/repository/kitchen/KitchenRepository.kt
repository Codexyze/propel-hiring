package com.scrymz.propel_hiring.domain.repository.kitchen

import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow

interface KitchenRepository {
    suspend fun getKitchens(): Flow<ResultState<List<Kitchen>>>
    suspend fun getKitchenById(id: String): Flow<ResultState<Kitchen>>
}
