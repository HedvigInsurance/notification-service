package com.hedvig.notificationService.customerio

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.Locale

class CustomerioServiceForwardUpdateTest {

    val objectMapper = ObjectMapper()

    @get:Rule
    val thrown = ExpectedException.none()

    @MockK
    lateinit var contractLoader: ContractLoader

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    private val repository =
        InMemoryCustomerIOStateRepository()

    lateinit var customerIOMockNorway: CustomerioMock
    lateinit var customerIOMockSweden: CustomerioMock
    lateinit var router: CustomerioService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        customerIOMockSweden = CustomerioMock(objectMapper)
        customerIOMockNorway = CustomerioMock(objectMapper)

        router = CustomerioService(
            WorkspaceSelector(
                contractLoader,
                memberServiceImpl
            ),
            repository,
            mapOf(
                Workspace.SWEDEN to customerIOMockSweden,
                Workspace.NORWAY to customerIOMockNorway
            ),
            mockk(),
            mockk()
        )
    }

    @Test
    internal fun `route message to swedish workspace`() {

        every { contractLoader.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        router.updateCustomerAttributes("1337", mapOf())
        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    internal fun `route message to norwegian workspace`() {
        every { contractLoader.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        router.updateCustomerAttributes("1337", mapOf())
        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `send to sweden when locale country is se and no market is found`() {

        every { contractLoader.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("sv", "se")

        router.updateCustomerAttributes("1337", mapOf())

        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    fun `send to norway when locale country is norway and no market is found`() {

        every { contractLoader.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("nb", "NO")

        router.updateCustomerAttributes("1337", mapOf())

        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `throw exception when member-service returns unsupported locale`() {

        every { contractLoader.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("en", "gb")

        thrown.expect(WorkspaceNotFound::class.java)
        router.updateCustomerAttributes("21313", mapOf())
    }
}
