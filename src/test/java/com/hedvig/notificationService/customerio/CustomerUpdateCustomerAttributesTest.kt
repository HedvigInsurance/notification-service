package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CustomerUpdateCustomerAttributesTest {

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK(relaxed = true)
    lateinit var seCustomerioClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var noCustomerIoClient: CustomerioClient

    @MockK
    lateinit var contractLoader: ContractLoader

    @MockK
    lateinit var sut: CustomerioService

    private val repository =
        InMemoryCustomerIOStateRepository()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        sut = CustomerioService(
            workspaceSelector,
            repository,
            mapOf(
                Workspace.SWEDEN to seCustomerioClient,
                Workspace.NORWAY to noCustomerIoClient
            ),
            mockk(),
            mockk()
        )
    }

    @Test
    fun `update attribute with localdate is replaced`() {
        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.SWEDEN

        sut.updateCustomerAttributes("8080", mapOf("someKey" to LocalDate.of(2020, 8, 17)))

        val someKeyTimestamp = 1597615200L // UTC 2020-08-16 22:00 Stockholm time 2020-08-17 00:00
        verify { seCustomerioClient.updateCustomer("8080", mapOf("someKey" to someKeyTimestamp)) }
    }
}
