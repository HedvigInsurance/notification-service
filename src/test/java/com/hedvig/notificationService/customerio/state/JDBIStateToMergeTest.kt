package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import java.time.LocalDate

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIStateToMergeTest(@Autowired val jdbi: Jdbi) {

    lateinit var repository: JDBIRepository

    @BeforeEach
    fun setup() {
        repository = JDBIRepository(jdbi)
    }

    @Test
    fun `return row with activation date trigger is set`() {

        val state = makeCustomerioState(activationDateTriggerAt = LocalDate.now())
        repository.save(state)

        val rows = repository.statesWithTriggers()

        assertThat(rows.count()).isEqualTo(1)
    }

    @Test
    fun `return row where contract created trigger is set`() {

        val state = makeCustomerioState(contractCreatedTriggerAt = Instant.now())
        repository.save(state)

        val rows = repository.statesWithTriggers()

        assertThat(rows.count()).isEqualTo(1)
    }

    @Test
    fun `return row where start date updated trigger is set`() {

        val state = makeCustomerioState(startDateUpdatedTriggerAt = Instant.now())
        repository.save(state)

        val rows = repository.statesWithTriggers()

        assertThat(rows.count()).isEqualTo(1)
    }
}
