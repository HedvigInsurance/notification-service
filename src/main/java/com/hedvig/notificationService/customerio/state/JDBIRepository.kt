package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import java.time.Instant

class JDBIRepository(
    val jdbi: Jdbi
) : CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {

        jdbi.withHandle<Int, RuntimeException> {
            val stmt =
                """
INSERT INTO customerio_state (
    |member_id, 
    |sent_tmp_sign_event, 
    |underwriter_first_sign_attributes_update,
    |activation_date_trigger_at
|)
|VALUES (:memberId,
        |:sentTmpSignEvent,
        |:underwriterFirstSignAttributesUpdate,
        |:activationDateTriggerAt
|)
|ON CONFLICT (member_id) DO
        |UPDATE
        |SET sent_tmp_sign_event = :sentTmpSignEvent,
        |activation_date_trigger_at = :activationDateTriggerAt
|""".trimMargin()

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
