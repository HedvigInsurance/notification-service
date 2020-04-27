package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
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
        sut.startDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId"), time)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedAt).isEqualTo(time)
    }
}
