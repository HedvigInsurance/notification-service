package com.hedvig.notificationService.service.request

import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Repository

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
                    request_id
                ) VALUES (
                    :requestId
                )
            """.trimIndent()
            it.createUpdate(statment)
                .bind("requestId", requestId)
                .execute()
        }
    }
}
