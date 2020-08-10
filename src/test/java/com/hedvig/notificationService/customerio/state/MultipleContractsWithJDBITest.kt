package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
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
import java.time.temporal.ChronoUnit
import kotlin.streams.toList

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class MultipleContractsWithJDBITest(@Autowired val jdbi: Jdbi) {

    val repository = JDBIRepository(jdbi = jdbi)

    @Test
    fun `multiple contracts`() {

        val state = makeCustomerioState()
        state.createContract("FirstContract", Instant.now(), null)
        state.createContract("SercondContract", Instant.now(), null)
        repository.save(state)

        val rows = jdbi.withHandle<Int, java.lang.RuntimeException> {
            it.createQuery("select count(1) from contract_state").mapTo(Int::class.java).first()
        }

        assertThat(rows).isEqualTo(2)
    }

    @Test
    fun `loads multiple contracts`() {

        val state = makeCustomerioState()
        state.createContract("FirstContract", Instant.now(), null)
        state.createContract("SercondContract", Instant.now(), null)
        repository.save(state)

        val newState = repository.findByMemberId(state.memberId)

        assertThat(newState!!.contracts).hasSize(2)
        assertThat(newState.contracts[0]).isEqualTo(state.contracts[0])
        assertThat(newState.contracts[1]).isEqualTo(state.contracts[1])
    }

    @Test
    fun `return contracts with shouldUpdate`() {
        val state = makeCustomerioState(
            underwriterFirstSignAttributesUpdate = Instant.now().minus(1, ChronoUnit.DAYS)
        )
        state.createContract("FirstContract", Instant.now(), null)
        state.createContract("SercondContract", Instant.now(), null)
        repository.save(state)

        val result = repository.shouldUpdate(Instant.now()).toList().first()
        assertThat(result.contracts).hasSize(2)
        assertThat(result.contracts[0]).isEqualTo(state.contracts[0])
        assertThat(result.contracts[1]).isEqualTo(state.contracts[1])
    }
}
