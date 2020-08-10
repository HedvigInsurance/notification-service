package com.hedvig.notificationService.customerio.state

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinMapper
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.jdbi.v3.core.mapper.RowMapperFactory
import org.jdbi.v3.core.result.RowView
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.stream.Stream

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
                val findStmt = "SELECT COUNT(*) count FROM contract_state WHERE contract_id = :contractId"
                val result = it.createQuery(findStmt)
                    .bind("contractId", contract.contractId)
                    .map { row -> row.getColumn("count", java.lang.Integer::class.java) }
                    .findFirst()

                if (result.filter { count -> count >= 1 }.isPresent) {
                    return@withHandle 0
                }

                val stmt =
                    """
  INSERT INTO contract_state (
      contract_id,
      member_id
  )
  VALUES (:contractId,
          :memberId
  )
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
            registerRowMappers(it)
            it.createQuery(
                """
$SELECT_STATE_AND_CONTRACTS     
from customerio_state cs
LEFT JOIN contract_state c ON c.member_id = cs.member_id
where cs.member_id = :memberId
            """.trimIndent()
            )
                .bind("memberId", memberId)
                .reduceRows(this::contractStateReducer)
                .findFirst()
        }.orElse(null)
    }

    override fun shouldUpdate(byTime: Instant): Stream<CustomerioState> {
        return jdbi.withHandleUnchecked {
            registerRowMappers(it)
            it.createQuery(
                """
                    WITH contract_triggers AS (
                     SELECT member_id
                     FROM contract_state
                     GROUP BY member_id)
                    $SELECT_STATE_AND_CONTRACTS 
                    FROM customerio_state cs
                    LEFT JOIN contract_state c ON c.member_id = cs.member_id
                    LEFT JOIN contract_triggers ct on ct.member_id = cs.member_id
                    WHERE 
                        (cs.underwriter_first_sign_attributes_update <= :byTime AND cs.sent_tmp_sign_event = false)
                """.trimIndent()
            )
                .bind("byTime", byTime)
                .reduceRows(this::contractStateReducer)
        }
    }

    private fun contractStateReducer(
        map: MutableMap<String, CustomerioState>,
        rw: RowView
    ) {
        val contact: CustomerioState = map.computeIfAbsent(
            rw.getColumn("cs_member_id", String::class.java)
        ) { rw.getRow(CustomerioState::class.java) }

        if (rw.getColumn("c_contract_id", String::class.java) != null) {
            val element = rw.getRow(ContractState::class.java)

            contact.contracts.add(element)
        }
    }

    private fun registerRowMappers(handle: Handle) {
        handle.registerRowMapper(
            RowMapperFactory.of(
                CustomerioState::class.java,
                KotlinMapper(CustomerioState::class.java, "cs")
            )
        )
        handle.registerRowMapper(
            RowMapperFactory.of(
                ContractState::class.java,
                KotlinMapper(ContractState::class.java, "c")
            )
        )
    }
}

val SELECT_STATE_AND_CONTRACTS = """
SELECT
    cs.member_id as cs_member_id,
    cs.sent_tmp_sign_event as cs_sent_tmp_sign_event,
    cs.underwriter_first_sign_attributes_update as cs_underwriter_first_sign_attributes_update,
    cs.activation_date_trigger_at as cs_activation_date_trigger_at,
    cs.contract_created_trigger_at as cs_contract_created_trigger_at,
    cs.start_date_updated_trigger_at as cs_start_date_updated_trigger_at,
    c.contract_id as c_contract_id
"""
