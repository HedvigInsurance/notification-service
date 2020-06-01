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
    fun `after insert two saves repo row count is 1`() {

        val state = makeCustomerioState()
        repository.save(state)
        repository.save(state)

        val rows = jdbi.withHandle<Int, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Int::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun `verify memberId is persisted`() {

        val state = makeCustomerioState("1337")
        repository.save(state)

        val memberId = jdbi.withHandle<String, RuntimeException> {
            it.createQuery("select member_id from customerio_state").mapTo(String::class.java).first()
        }

        assertThat(memberId).isEqualTo("1337")
    }
    /**
     *
     * save == load
     * save assigns id
     * dedicated save vs update
     * Later
     * implement unsaved type for ID field
     *
     */
}

fun makeCustomerioState(memberId: String = "1338"): CustomerioState {
    return CustomerioState(memberId)
}
