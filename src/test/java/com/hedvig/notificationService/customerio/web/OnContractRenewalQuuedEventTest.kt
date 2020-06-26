package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class OnContractRenewalQuuedEventTest {

    val repo = InMemoryCustomerIOStateRepository()
    lateinit var sut: EventHandler

    @BeforeEach
    fun setup() {
        sut = EventHandler(repo, ConfigurationProperties(), mapOf(), mockk(), mockk())
    }

    @Test
    fun `contractRenewalQueuedTriggerAt set`() {
        val aInstant = Instant.now()
        sut.onContractRenewalQueued(ContractRenewalQueuedEvent("aContract", "1337"), aInstant)

        val contract = repo.data["1337"]!!.contracts.find { it.contractId == "aContract" }
        assertThat(contract!!.contractRenewalQueuedTriggerAt).isEqualTo(aInstant)
    }

    @Test
    fun `existing contract exists`() {
        val customerioState = CustomerioState("1337")
        customerioState.createContract("aContract", Instant.now(), null)
        repo.save(customerioState)

        val aInstant = Instant.now()
        sut.onContractRenewalQueued(ContractRenewalQueuedEvent("aContract", "1337"), aInstant)

        val contract = repo.data["1337"]!!.contracts.find { it.contractId == "aContract" }
        assertThat(contract!!.contractRenewalQueuedTriggerAt).isEqualTo(aInstant)
    }

    @Test
    fun `existing contract with other id exists`() {

        val customerioState = CustomerioState("1337")
        customerioState.createContract("aOtherContract", Instant.now(), null)
        repo.save(customerioState)

        val aInstant = Instant.now()
        sut.onContractRenewalQueued(ContractRenewalQueuedEvent("aContract", "1337"), aInstant)

        val contract = repo.data["1337"]!!.contracts.find { it.contractId == "aContract" }
        assertThat(contract!!.contractRenewalQueuedTriggerAt).isEqualTo(aInstant)
    }
}
