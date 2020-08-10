package com.hedvig.notificationService.customerio.state

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
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
import java.time.ZoneId
import kotlin.streams.toList

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryFindTriggersToUpdateTest(@Autowired val jdbi: Jdbi) {

    val repository = JDBIRepository(jdbi = jdbi)

    @Test
    internal fun `findStatesToUpdate with empty db`() {
        val result = repository.shouldUpdate(Instant.parse("2020-06-01T13:41:39.739783Z"))
        assertThat(result.toList()).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("makeData")
    fun testShouldUpdate(state: CustomerioState, timestamp: Instant, expectedResultSize: Int) {
        repository.save(state)

        val result = repository.shouldUpdate(timestamp)

        assertThat(result.toList()).all {
            hasSize(expectedResultSize)
        }
    }

    companion object {
        @JvmStatic
        fun makeData(): List<Arguments> {
            return listOf(
                run {
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = null),
                        Instant.parse("2020-06-01T13:41:39.739783Z"),
                        0
                    )
                },
                run {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt,
                        1
                    )
                },
                run {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt.plusMillis(1),
                        1
                    )
                },
                run {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt.minusMillis(1),
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = false
                        ),
                        timestamp.minusMillis(1),
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = false
                        ),
                        timestamp,
                        1
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = false
                        ),
                        timestamp.plusMillis(1),
                        1
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = true
                        ),
                        timestamp.minusMillis(1),
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = true
                        ),
                        timestamp,
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            underwriterFirstSignAttributesUpdate = timestamp,
                            sentTmpSignEvent = true
                        ),
                        timestamp.plusMillis(1),
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            startDateUpdatedTriggerAt = timestamp
                        ),
                        timestamp.minusMillis(1),
                        0
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            startDateUpdatedTriggerAt = timestamp
                        ),
                        timestamp,
                        1
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            startDateUpdatedTriggerAt = timestamp
                        ),
                        timestamp.plusMillis(1),
                        1
                    )
                },
                // activationDateTriggerAt
                run {
                    val timestamp = LocalDate.of(2020, 6, 1)
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            activationDateTriggerAt = timestamp
                        ),
                        timestamp.minusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant(),
                        0
                    )
                },
                run {
                    val timestamp = LocalDate.of(2020, 6, 1)
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            activationDateTriggerAt = timestamp
                        ),
                        timestamp.atStartOfDay(ZoneId.of("UTC")).toInstant(),
                        1
                    )
                },
                run {
                    val timestamp = LocalDate.of(2020, 6, 1)
                    Arguments.of(
                        CustomerioState(
                            "1337",
                            activationDateTriggerAt = timestamp
                        ),
                        timestamp.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant(),
                        1
                    )
                },
                run {
                    val timestamp = Instant.parse("2020-06-01T13:41:39.739783Z")
                    val state = makeCustomerioState()
                    Arguments.of(
                        state,
                        timestamp.minusSeconds(1),
                        0
                    )
                }
            )
        }
    }
}
