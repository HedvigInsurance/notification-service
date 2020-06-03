package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ContractCreatedEventCreatesContractTest {

    val configurationProperties = ConfigurationProperties()
    val repo = InMemoryCustomerIOStateRepository()

    @BeforeEach
    fun setup() {
        configurationProperties.useNorwayHack = false
    }

    @Test
    internal fun `first test`() {

        val sut = EventHandler(repo, configurationProperties)
        sut.onContractCreatedEvent(
            ContractCreatedEvent("theContractId", "aMemberId", null),
            Instant.parse("2020-06-03T08:02:39.403803Z")
        )

        assertThat(repo.findByMemberId("aMemberId"))
            .isNotNull()
            .transform { it.contracts }
            .extracting { it.contractId }
            .containsExactly("theContractId")
    }
}
