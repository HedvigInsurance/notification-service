package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class OnContractRenewalQuuedEventTest {

    val repo = InMemoryCustomerIOStateRepository()

    @Test
    fun `aa`() {
        val sut = EventHandler(repo, ConfigurationProperties())

        val aInstant = Instant.now()
        sut.onContractRenewalQueued(ContractRenewalQueuedEvent("aContract", "1337"), aInstant)

        val contract = repo.data["1337"]!!.contracts.find { it.contractId == "aContract" }
        assertThat(contract!!.contractRenewalQueuedTriggerAt).isEqualTo(aInstant)
    }
}
