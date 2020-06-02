package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isEmpty
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

@ExtendWith(SpringExtension::class)
@DataJdbcTest()
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(
    classes = [JDBIConfiguration::class],
    initializers = [ConfigFileApplicationContextInitializer::class]
)
class JDBIRepositoryFindStatesToUpdateTest(@Autowired val jdbi: Jdbi) {
    @Test
    internal fun `findStatesToUpdate with empty db`() {

        val repository = JDBIRepository(jdbi = jdbi)

        val result = repository.shouldUpdate(Instant.parse("2020-06-01T13:41:39.739783Z"))
        assertThat(result).isEmpty()
    }
}
