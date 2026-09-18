package com.scrymz.propel_hiring.domain.usecase.subscription

import com.scrymz.propel_hiring.domain.model.subscription.SubscriptionState
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSubscriptionStateUseCase @Inject constructor(
    private val repository: SubscriptionRepository
) {
    operator fun invoke(): Flow<SubscriptionState> {
        return repository.getSubscriptionState()
    }
}
