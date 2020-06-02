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
import java.util.stream.Stream

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryFindStatesToUpdateTest(@Autowired val jdbi: Jdbi) {

    val repository = JDBIRepository(jdbi = jdbi)

    @Test
    internal fun `findStatesToUpdate with empty db`() {
        val result = repository.shouldUpdate(Instant.parse("2020-06-01T13:41:39.739783Z"))
        assertThat(result).isEmpty()
    }

    @Test
    internal fun `find one contract created trigger with bytime equals`() {

        val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
        repository.save(CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt))

        val result = repository.shouldUpdate(contractCreatedTriggerAt)
        assertThat(result).all {
            hasSize(1)
        }
    }

    @Test
    internal fun `find one contract created trigger with bytime bigger`() {

        val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
        repository.save(CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt))

        val result = repository.shouldUpdate(contractCreatedTriggerAt.plusMillis(1))
        assertThat(result).all {
            hasSize(1)
        }
    }

    @Test
    internal fun `find one contract created trigger with bytime smaller`() {

        val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
        repository.save(CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt))

        val result = repository.shouldUpdate(contractCreatedTriggerAt.minusMillis(1))
        assertThat(result).all {
            hasSize(0)
        }
    }

    @Test
    internal fun `find one contract created trigger with contract created at null`() {

        val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
        repository.save(
            CustomerioState("1337", contractCreatedTriggerAt = null)
        )

        val result = repository.shouldUpdate(contractCreatedTriggerAt)
        assertThat(result).all {
            hasSize(0)
        }
    }

    @ParameterizedTest
    @MethodSource("makeData")
    fun testShouldUpdate(state: CustomerioState, timestamp: Instant, expectedResultSize: Int) {
        repository.save(state)

        val result = repository.shouldUpdate(timestamp)

        assertThat(result).all {
            hasSize(expectedResultSize)
        }
    }

    companion object {
        @JvmStatic
        fun makeData(): Stream<Arguments> {
            return Stream.of(
                {
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = null),
                        Instant.parse("2020-06-01T13:41:39.739783Z"),
                        0
                    )
                },
                {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt,
                        1
                    )
                },
                {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt.plusMillis(1),
                        1
                    )
                },
                {
                    val contractCreatedTriggerAt = Instant.parse("2020-06-01T13:41:39.739783Z")
                    Arguments.of(
                        CustomerioState("1337", contractCreatedTriggerAt = contractCreatedTriggerAt),
                        contractCreatedTriggerAt.minusMillis(1),
                        0
                    )
                }
            )
                .map { it() }
        }
    }
}
