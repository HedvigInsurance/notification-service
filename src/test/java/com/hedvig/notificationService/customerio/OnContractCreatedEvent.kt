package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.customerio.web.ContractCreatedEvent
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.time.Instant

class OnContractCreatedEvent {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK
    lateinit var customerioClient: CustomerioClient

    private val repository =
        InMemoryCustomerIOStateRepository()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun onContractCreatedEvent() {
        val sut = CustomerioService(
            workspaceSelector,
            repository,
            CustomerioEventCreatorImpl(productPricingFacade),
            mapOf(
                Workspace.SWEDEN to customerioClient,
                Workspace.NORWAY to customerioClient
            ),
            productPricingFacade
        )

        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.contractCreatedEvent(ContractCreatedEvent("someEventId", "1337"), time)

        assertThat(repository.data["1337"]?.contractCreatedAt).isEqualTo(time)
    }
}
