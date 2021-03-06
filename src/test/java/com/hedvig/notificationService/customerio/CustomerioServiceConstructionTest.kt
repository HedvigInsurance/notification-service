package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class CustomerioServiceConstructionTest {

    @get:Rule
    var exceptionRule: ExpectedException = ExpectedException.none()

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK
    lateinit var customerioClient: CustomerioClient

    @MockK
    lateinit var contractLoader: ContractLoader

    private val repository =
        InMemoryCustomerIOStateRepository()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Throw if no markets are passed in the constructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        CustomerioService(workspaceSelector, repository, mapOf(), mockk(), mockk())
    }

    @Test
    fun `Throw if not all markets are passed in the constructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        CustomerioService(
            workspaceSelector,
            repository,
            mapOf(Workspace.SWEDEN to customerioClient),
            mockk(),
            mockk()
        )
    }

    @Test
    fun `Do not throw when all markets are passed in the constructor`() {
        CustomerioService(
            workspaceSelector,
            repository,
            mapOf(
                Workspace.SWEDEN to customerioClient,
                Workspace.NORWAY to customerioClient,
                Workspace.DENMARK to customerioClient
            ),
            mockk(),
            mockk()
        )
    }
}
