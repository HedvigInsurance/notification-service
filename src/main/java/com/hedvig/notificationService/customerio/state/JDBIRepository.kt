package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.core.mapper.reflect.FieldMapper
import java.time.Instant

class JDBIRepository(
    private val jdbi: Jdbi
) : CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {

        jdbi.withHandle<Int, RuntimeException> {
            val stmt =
                """
INSERT INTO customerio_state (
    |member_id, 
    |sent_tmp_sign_event, 
    |underwriter_first_sign_attributes_update,
    |activation_date_trigger_at,
    |contract_created_trigger_at,
    |start_date_updated_trigger_at
|)
|VALUES (:memberId,
        |:sentTmpSignEvent,
        |:underwriterFirstSignAttributesUpdate,
        |:activationDateTriggerAt,
        |:contractCreatedTriggerAt,
        |:startDateUpdatedTriggerAt
|)
|ON CONFLICT (member_id) DO
        |UPDATE
        |SET sent_tmp_sign_event = :sentTmpSignEvent,
        |activation_date_trigger_at = :activationDateTriggerAt,
        |contract_created_trigger_at = :contractCreatedTriggerAt,
        |start_date_updated_trigger_at = :startDateUpdatedTriggerAt
|""".trimMargin()

            val update = it.createUpdate(stmt)
            update.bindBean(customerioState)

            update.execute()
        }
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        return jdbi.withHandleUnchecked {
            it.registerRowMapper(FieldMapper.factory(CustomerioState::class.java))
            it.createQuery(
                """
                SELECT * from customerio_state where member_id = :memberId
            """.trimIndent()
            )
                .bind("memberId", memberId)
                .mapTo(CustomerioState::class.java)
                .findFirst()
        }.orElse(null)
    }

    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState> {
        return jdbi.withHandleUnchecked {
            it.registerRowMapper(FieldMapper.factory(CustomerioState::class.java))
                .createQuery(
                    """
                    SELECT * FROM customerio_state WHERE
                    (contract_created_trigger_at <= :byTime)
                """.trimIndent()
                )
                .bind("byTime", byTime)
                .mapTo(CustomerioState::class.java)
        }.toList()
    }
}
