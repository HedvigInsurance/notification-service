package com.hedvig.notificationService.customerio.state

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.withHandleUnchecked
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.time.LocalDate
import java.util.stream.Stream

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryTest(@Autowired val jdbi: Jdbi) {

    lateinit var repository: JDBIRepository

    @BeforeEach
    fun setup() {
        repository = JDBIRepository(jdbi)
    }

    @Test
    fun `simple jdbi query test`() {
        val result = jdbi.withHandle<Int, RuntimeException> { it ->
            it.createQuery("select 1 as number")
                .mapTo(Int::class.java).first()
        }

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `after insert repo row count is 1`() {

        val state = makeCustomerioState()
        repository.save(state)

        val rows = jdbi.withHandle<Int, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Int::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun `after two saves with same object row count is 1`() {

        val state = makeCustomerioState()
        repository.save(state)
        repository.save(state)

        val rows = jdbi.withHandle<Int, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Int::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun `save and load`() {
        val state = makeCustomerioState(
            "aMemberId",
            activationDateTriggerAt = LocalDate.of(2020, 1, 1),
            startDateUpdatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z"),
            contractCreatedTriggerAt = Instant.parse("2020-05-01T13:41:39.739783Z"),
            sentTmpSignEvent = true,
            underwriterFirstSignAttributesUpdate = Instant.parse("2020-04-01T13:41:39.739783Z")
        )
        repository.save(state)

        val customerioState = repository.findByMemberId("aMemberId")

        assertThat(customerioState).isNotNull().all {
            transform { it.memberId }.isEqualTo("aMemberId")
            transform { it.activationDateTriggerAt }.isEqualTo(LocalDate.of(2020, 1, 1))
            transform { it.startDateUpdatedTriggerAt }.isEqualTo(Instant.parse("2020-06-01T13:41:39.739783Z"))
            transform { it.contractCreatedTriggerAt }.isEqualTo(Instant.parse("2020-05-01T13:41:39.739783Z"))
            transform { it.sentTmpSignEvent }.isEqualTo(true)
            transform { it.underwriterFirstSignAttributesUpdate }.isEqualTo(Instant.parse("2020-04-01T13:41:39.739783Z"))
        }
    }

    @Test
    fun `save and load with multiple contract states with multiple saves`() {
        val state = makeCustomerioState()
        state.contracts.add(ContractState("a"))
        state.contracts.add(ContractState("b"))
        repository.save(state)
        repository.save(state)

        val rows = jdbi.withHandle<Int, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Int::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
    }

    companion object {
        @JvmStatic
        fun makeTestData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("member_id", "1337", makeCustomerioState("1337")),
                Arguments.of(
                    "activation_date_trigger_at",
                    LocalDate.of(2020, 5, 31),
                    makeCustomerioState(activationDateTriggerAt = LocalDate.of(2020, 5, 31))
                ),
                Arguments.of(
                    "activation_date_trigger_at",
                    LocalDate.of(2020, 5, 31),
                    makeCustomerioState(activationDateTriggerAt = LocalDate.of(2020, 5, 31))
                ),
                Arguments.of(
                    "contract_created_trigger_at",
                    Instant.parse("2020-06-01T13:41:39.739783Z"),
                    makeCustomerioState(contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z"))
                ),
                Arguments.of(
                    "underwriter_first_sign_attributes_update",
                    Instant.parse("2020-06-01T13:41:39.739783Z"),
                    makeCustomerioState(underwriterFirstSignAttributesUpdate = Instant.parse("2020-06-01T13:41:39.739783Z"))
                ),
                Arguments.of(
                    "sent_tmp_sign_event",
                    true,
                    makeCustomerioState(sentTmpSignEvent = true)
                ),
                Arguments.of(
                    "sent_tmp_sign_event",
                    false,
                    makeCustomerioState(sentTmpSignEvent = false)
                ),
                Arguments.of(
                    "start_date_updated_trigger_at",
                    Instant.parse("2020-06-01T13:41:39.739783Z"),
                    makeCustomerioState(startDateUpdatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z"))
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("makeTestData")
    fun `verify field activationDateTriggerAt persisted`(coliumnName: String, value: Any, state: CustomerioState) {

        repository.save(state)

        val dbValue = jdbi.withHandleUnchecked {
            it.createQuery("select $coliumnName from customerio_state").mapTo(value::class.java)
        }

        assertThat(dbValue.first()).isEqualTo(value)
    }
    /**
     *
     * save == load
     * Later
     * dedicated save vs update
     * implement unsaved type for ID field
     *
     */
}

fun makeCustomerioState(
    memberId: String = "1338",
    activationDateTriggerAt: LocalDate? = null,
    contractCreatedTriggerAt: Instant? = null,
    underwriterFirstSignAttributesUpdate: Instant? = null,
    sentTmpSignEvent: Boolean = false,
    startDateUpdatedTriggerAt: Instant? = null
): CustomerioState {
    return CustomerioState(
        memberId,
        activationDateTriggerAt = activationDateTriggerAt,
        contractCreatedTriggerAt = contractCreatedTriggerAt,
        underwriterFirstSignAttributesUpdate = underwriterFirstSignAttributesUpdate,
        sentTmpSignEvent = sentTmpSignEvent,
        startDateUpdatedTriggerAt = startDateUpdatedTriggerAt
    )
}
