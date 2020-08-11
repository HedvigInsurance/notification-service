package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Repository
import java.time.Instant

interface EventHashRepository {
    fun save(memberId: String, hash: String)
}

@Repository
class EventHashRepositoryImpl(
    val jdbi: Jdbi
): EventHashRepository {
    override fun save(memberId: String, hash: String) {
        jdbi.withHandle<Int, RuntimeException> {
            val stmt =
                """
                    INSERT INTO event_hash (
                        member_id,
                        hash,
                        created_at
                    )
                    VALUES (
                        :memberId,
                        :hash,
                        :createdAt
                    )
                    ON CONFLICT (member_id, hash) DO NOTHING
                """

            it.createUpdate(stmt)
                .bind("memberId", memberId)
                .bind("hash", memberId)
                .bind("createdAt", Instant.now())
                .execute()
        }
    }
}

data class EventHash(
    val memberId: String,
    val hash: String,
    val createdAt: Instant
)