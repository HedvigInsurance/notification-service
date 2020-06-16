package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.core.mapper.RowMapperFactory
import org.jdbi.v3.core.result.RowView
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class JDBIRepository(
    private val jdbi: Jdbi
) : CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {

        jdbi.withHandle<Int, RuntimeException> {
            val stmt =
                """
INSERT INTO customerio_state (
    member_id, 
    sent_tmp_sign_event, 
    underwriter_first_sign_attributes_update,
    activation_date_trigger_at,
    contract_created_trigger_at,
    start_date_updated_trigger_at
)
VALUES (:memberId,
        :sentTmpSignEvent,
        :underwriterFirstSignAttributesUpdate,
        :activationDateTriggerAt,
        :contractCreatedTriggerAt,
        :startDateUpdatedTriggerAt
)
ON CONFLICT (member_id) DO
        UPDATE
        SET sent_tmp_sign_event = :sentTmpSignEvent,
        activation_date_trigger_at = :activationDateTriggerAt,
        contract_created_trigger_at = :contractCreatedTriggerAt,
        start_date_updated_trigger_at = :startDateUpdatedTriggerAt
""".trimMargin()

            val update = it.createUpdate(stmt)
            update.bindBean(customerioState)

            update.execute()
        }

        insertOrUpdateContractState(customerioState)
    }

    private fun insertOrUpdateContractState(customerioState: CustomerioState) {
        customerioState.contracts.forEach { contract ->
            jdbi.withHandle<Int, RuntimeException> {
                val stmt =
                    """
  INSERT INTO contract_state (
      contract_id,
      member_id,
      renewal_date,
      contract_renewal_queued_trigger_at
  )
  VALUES (:contractId,
          :memberId,
          :renewalDate,
          :contractRenewalQueuedTriggerAt
  )
  ON CONFLICT (contract_id) DO
          UPDATE
          SET renewal_date = :renewalDate,
          contract_renewal_queued_trigger_at = :contractRenewalQueuedTriggerAt
  """.trimMargin()

                val update = it.createUpdate(stmt)
                update.bindBean(contract)
                update.bind("memberId", customerioState.memberId)
                update.execute()
            }
        }
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        return jdbi.withHandleUnchecked {
            it.createQuery(
                """
SELECT 
    cs.member_id as cs_member_id,
    cs.sent_tmp_sign_event as cs_sent_tmp_sign_event, 
    cs.underwriter_first_sign_attributes_update as cs_underwriter_first_sign_attributes_update,
    cs.activation_date_trigger_at as cs_activation_date_trigger_at,
    cs.contract_created_trigger_at as cs_contract_created_trigger_at,
    cs.start_date_updated_trigger_at as cs_start_date_updated_trigger_at,
    c.contract_id as c_contract_id,
    c.renewal_date as c_renewal_date,
    c.contract_renewal_queued_trigger_at as c_contract_renewal_queued_trigger_at     
from customerio_state cs
LEFT JOIN contract_state c ON c.member_id = cs.member_id
where cs.member_id = :memberId
            """.trimIndent()
            )
                .bind("memberId", memberId)
                .registerRowMapper(
                    RowMapperFactory.of(
                        CustomerioState::class.java,
                        KotlinMapper(CustomerioState::class.java, "cs")
                    )
                )
                .registerRowMapper(
                    RowMapperFactory.of(
                        ContractState::class.java,
                        KotlinMapper(ContractState::class.java, "c")
                    )
                )
                .reduceRows { map: MutableMap<String, CustomerioState>, rw: RowView ->
                    val contact: CustomerioState = map.computeIfAbsent(
                        rw.getColumn("cs_member_id", String::class.java)
                    ) { rw.getRow(CustomerioState::class.java) }

                    if (rw.getColumn("c_contract_id", String::class.java) != null) {
                        val element = rw.getRow(ContractState::class.java)

                        contact.contracts.add(element)
                    }
                }.findFirst()
        }.orElse(null)
    }

    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState> {
        return jdbi.withHandleUnchecked {
            it.registerRowMapper(KotlinMapper(CustomerioState::class.java))
                .createQuery(
                    """
                    SELECT * 
                    FROM customerio_state
                    WHERE
                        (contract_created_trigger_at <= :byTime)
                    OR 
                        (underwriter_first_sign_attributes_update <= :byTime AND sent_tmp_sign_event = false)
                    OR 
                        (start_date_updated_trigger_at <= :byTime)
                    OR
                        (activation_date_trigger_at <= :byTime)
                """.trimIndent()
                )
                .bind("byTime", byTime)
                .mapTo(CustomerioState::class.java)
        }.toList()
    }
}
