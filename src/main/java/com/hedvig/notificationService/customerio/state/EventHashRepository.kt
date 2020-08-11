package com.hedvig.notificationService.customerio.state

import org.springframework.stereotype.Repository

interface EventHashRepository {
    fun save(eventHash: EventHash)
}

@Repository
class EventHashRepositoryImpl: EventHashRepository {
    override fun save(eventHash: EventHash) {
        TODO("Not yet implemented")
    }
}

data class EventHash(
    val memberId: String,
    val hash: String
)