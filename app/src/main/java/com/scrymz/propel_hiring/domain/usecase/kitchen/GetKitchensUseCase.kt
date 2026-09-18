package com.scrymz.propel_hiring.domain.usecase.kitchen

import com.scrymz.propel_hiring.domain.model.kitchen.Kitchen
import com.scrymz.propel_hiring.domain.repository.kitchen.KitchenRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetKitchensUseCase @Inject constructor(
    private val repository: KitchenRepository
) {
    suspend operator fun invoke(): Flow<ResultState<List<Kitchen>>> {
        return repository.getKitchens()
    }
}
