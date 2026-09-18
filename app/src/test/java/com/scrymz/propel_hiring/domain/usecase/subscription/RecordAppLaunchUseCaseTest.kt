package com.scrymz.propel_hiring.domain.usecase.subscription

import com.scrymz.propel_hiring.analytics.Analytics
import com.scrymz.propel_hiring.domain.model.subscription.SubscriptionState
import com.scrymz.propel_hiring.domain.repository.subscription.SubscriptionRepository
import com.scrymz.propel_hiring.domain.state.ResultState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordAppLaunchUseCaseTest {

    private class FakeSubscriptionRepository : SubscriptionRepository {
        var currentLaunchCount = 0

        override fun getSubscriptionState(): Flow<SubscriptionState> {
            return flowOf(SubscriptionState(isSubscribed = false, launchCount = currentLaunchCount))
        }

        override suspend fun incrementLaunchCount(): Flow<ResultState<Int>> = flow {
            currentLaunchCount++
            emit(ResultState.Success(currentLaunchCount))
        }

        override suspend fun processFakePurchase(): Flow<ResultState<Boolean>> = flow {
            emit(ResultState.Success(true))
        }
    }

    private class FakeAnalytics : Analytics {
        val events = mutableListOf<String>()
        override fun logEvent(eventName: String, params: Map<String, Any>) {
            events.add(eventName)
        }
    }

    @Test
    fun `invoke increments launch count and logs analytics event`() = runBlocking {
        val repo = FakeSubscriptionRepository()
        val analytics = FakeAnalytics()
        val useCase = RecordAppLaunchUseCase(repo, analytics)

        val result = useCase().first()

        assertTrue(result is ResultState.Success)
        assertEquals(1, (result as ResultState.Success).data)
        assertEquals(1, analytics.events.size)
        assertEquals("app_launched", analytics.events[0])
    }
}
