package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import java.time.Instant

class JDBIRepository(
    val jdbi: Jdbi
) : CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {

        jdbi.withHandle<Int, RuntimeException> {
            val stmt = """INSERT INTO customerio_state 
                |VALUES (:memberId, 
                |:sentTmpSignEvent,
                |:underwriterFirstSignAttributesUpdate)""".trimMargin()
            val update = it.createUpdate(stmt)
            update.bindBean(customerioState)

            update.execute()
        }
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        TODO("Not yet implemented")
    }

    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState> {
        TODO("Not yet implemented")
    }
}
