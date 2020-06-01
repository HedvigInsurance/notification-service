package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryTest {

    @Autowired
    lateinit var jdbi: Jdbi

    @Test
    fun `simple jdbi query test`() {
        val result = jdbi.withHandle<Integer, RuntimeException> { it ->
            it.createQuery("select 1 as number")
                .mapTo(Integer::class.java).findOnly()
        }

        assertThat(result).isEqualTo(1)
    }

    @Test
    fun `after insert repo row count is 1`() {
        val repository = JDBIRepository(jdbi)

        repository.save(CustomerioState("aMemberId"))

        var rows = jdbi.withHandle<Integer, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Integer::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
    }

    @Test
    fun `after insert two saves repo row count is 1`() {
        val repository = JDBIRepository(jdbi)

        val state = CustomerioState("aMemberId")
        repository.save(state)
        repository.save(state)

        var rows = jdbi.withHandle<Integer, java.lang.RuntimeException> {
            it.createQuery("select count(*) from customerio_state").mapTo(Integer::class.java).first()
        }

        assertThat(rows).isEqualTo(1)
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
