package com.hedvig.notificationService.service.request

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class HandledRequestRepositoryImpl(
    private val jdbi: Jdbi
) : HandledRequestRepository {
    override fun isRequestHandled(requestId: String): Boolean = jdbi.withHandle<Boolean, RuntimeException> {
        val optional = it
            .createQuery("""
            SELECT 1
            FROM handled_request 
            WHERE request_id = :requestId
        """.trimIndent())
            .bind("requestId", requestId)
            .mapTo(String::class.java)
            .findFirst()

        optional.isPresent
    }

    override fun storeHandledRequest(requestId: String) {
        jdbi.withHandle<Int, RuntimeException> {
            val statment = """
                INSERT INTO handled_request (
                    request_id,
                    created_at
                ) VALUES (
                    :requestId,
                    :createdAt
                )
            """.trimIndent()
            it.createUpdate(statment)
                .bind("requestId", requestId)
                .bind("createdAt", Instant.now())
                .execute()
        }
    }
}
