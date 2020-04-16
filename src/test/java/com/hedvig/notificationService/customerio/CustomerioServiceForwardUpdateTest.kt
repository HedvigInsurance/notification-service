package com.hedvig.notificationService.customerio

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioMock
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
    lateinit var productPricingFacade: ProductPricingFacade

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    private val repository = InMemoryCustomerIOStateRepository()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    internal fun `route message to swedish workspace`() {
        val customerIOMockSweden = CustomerioMock(objectMapper)
        val customerIOMockNorway = CustomerioMock(objectMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        val router =
            CustomerioService(
                WorkspaceSelector(
                    productPricingFacade,
                    memberServiceImpl
                ),
                repository,
                Workspace.SWEDEN to customerIOMockSweden,
                Workspace.NORWAY to customerIOMockNorway
            )

        router.updateCustomerAttributes("1337", mapOf())
        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    internal fun `route message to norwegian workspace`() {
        val customerIOMockNorway = CustomerioMock(objectMapper)
        val customerIOMockSweden = CustomerioMock(objectMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val router =
            CustomerioService(
                WorkspaceSelector(
                    productPricingFacade,
                    memberServiceImpl
                ),
                repository,
                Workspace.SWEDEN to customerIOMockSweden,
                Workspace.NORWAY to customerIOMockNorway
            )

        router.updateCustomerAttributes("1337", mapOf())
        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `send to sweden when locale country is se and no market is found`() {
        val customerIOMockNorway = CustomerioMock(objectMapper)
        val customerIOMockSweden = CustomerioMock(objectMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("sv", "se")

        val router = CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                memberServiceImpl
            ),
            repository,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        router.updateCustomerAttributes("1337", mapOf())

        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    fun `send to norway when locale country is norway and no market is found`() {
        val customerIOMockNorway = CustomerioMock(objectMapper)
        val customerIOMockSweden = CustomerioMock(objectMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("nb", "NO")

        val router = CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                memberServiceImpl
            ),
            repository,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        router.updateCustomerAttributes("1337", mapOf())

        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `throw exception when member-service returns unsupported locale`() {
        val customerIOMockNorway = CustomerioMock(objectMapper)
        val customerIOMockSweden = CustomerioMock(objectMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("en", "gb")

        val router = CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                memberServiceImpl
            ),
            repository,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        thrown.expect(WorkspaceNotFound::class.java)
        router.updateCustomerAttributes("21313", mapOf())
    }
}
