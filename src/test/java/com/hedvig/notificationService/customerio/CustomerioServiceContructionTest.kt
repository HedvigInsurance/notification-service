package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreator
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class CustomerioServiceContructionTest {

    @get:Rule
    var exceptionRule: ExpectedException = ExpectedException.none()

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK
    lateinit var customerioClient: CustomerioClient

    @MockK
    lateinit var eventCreator: CustomerioEventCreator

    private val repository =
        InMemoryCustomerIOStateRepository()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Throw if no markets are passed in the contructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        CustomerioService(workspaceSelector, repository, eventCreator, mapOf())
    }

    @Test
    fun `Throw if not all markets are passed in the contructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        CustomerioService(
            workspaceSelector,
            repository,
            eventCreator,
            mapOf(Workspace.SWEDEN to customerioClient)
        )
    }

    @Test
    fun `Do not throw when all markets are passed in the contructor`() {
        CustomerioService(
            workspaceSelector,
            repository,
            eventCreator,
            mapOf(
                Workspace.SWEDEN to customerioClient,
                Workspace.NORWAY to customerioClient
            )
        )
    }
}
