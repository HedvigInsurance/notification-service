package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.configuration.JDBIConfiguration
import org.jdbi.v3.core.Jdbi
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJdbcTest
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryTest {

    @Autowired
    lateinit var jdbi: Jdbi

    @Test
    fun test() {
        val result = jdbi.withHandle<Integer, RuntimeException> { it ->
            it.createQuery("select 1 as number")
                .mapTo(Integer::class.java).findOnly()
        }

        assertThat(result).isEqualTo(1)
    }
}
