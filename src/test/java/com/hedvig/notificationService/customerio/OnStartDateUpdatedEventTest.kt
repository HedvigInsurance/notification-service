package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import org.junit.Before
import org.junit.Test
import java.time.Instant

class OnStartDateUpdatedEventTest {

    lateinit var repo: InMemoryCustomerIOStateRepository

    @Before
    fun setup() {
        repo = InMemoryCustomerIOStateRepository()
    }

    @Test
    fun `on start date updated event`() {
        val sut = EventHandler(repo)
        val time = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId"), time)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedAt).isEqualTo(time)
    }

    @Test
    fun `on start date updated event when startDate extists`() {
        val sut = EventHandler(repo)

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        repo.save(CustomerioState("aMemberId", null, startDateUpdatedAt = timeOfFirstCall))

        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId"), timeOfFirstCall.plusMillis(3000))

        assertThat(repo.data["aMemberId"]?.startDateUpdatedAt).isEqualTo(timeOfFirstCall)
    }
}
