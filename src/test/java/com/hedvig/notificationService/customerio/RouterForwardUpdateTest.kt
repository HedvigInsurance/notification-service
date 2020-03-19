package com.hedvig.notificationService.customerio

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioMock
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.util.Locale
import kotlin.test.assertEquals

class RouterForwardUpdateTest {

    val objetMapper = ObjectMapper()

    @get:Rule
    val thrown = ExpectedException.none()

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    internal fun `route message to swedish workspace`() {
        val customerIOMockSweden = CustomerioMock(objetMapper)
        val customerIOMockNorway = CustomerioMock(objetMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        val router =
            Router(
                productPricingFacade,
                memberServiceImpl,
                Workspace.SWEDEN to customerIOMockSweden,
                Workspace.NORWAY to customerIOMockNorway
            )

        router.updateCustomer("1337", mapOf())
        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    internal fun `route message to norwegian workspace`() {
        val customerIOMockNorway = CustomerioMock(objetMapper)
        val customerIOMockSweden = CustomerioMock(objetMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val router =
            Router(
                productPricingFacade,
                memberServiceImpl,
                Workspace.SWEDEN to customerIOMockSweden,
                Workspace.NORWAY to customerIOMockNorway
            )

        router.updateCustomer("1337", mapOf())
        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `send to sweden when locale country is se and no market is found`() {
        val customerIOMockNorway = CustomerioMock(objetMapper)
        val customerIOMockSweden = CustomerioMock(objetMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("sv", "se")

        val router = Router(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        router.updateCustomer("1337", mapOf())

        assertEquals("1337", customerIOMockSweden.updates.first().first)
    }

    @Test
    fun `send to norway when locale country is norway and no market is found`() {
        val customerIOMockNorway = CustomerioMock(objetMapper)
        val customerIOMockSweden = CustomerioMock(objetMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("nb", "NO")

        val router = Router(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        router.updateCustomer("1337", mapOf())

        assertEquals("1337", customerIOMockNorway.updates.first().first)
    }

    @Test
    fun `throw exception when member-service returns unsupported locale`() {
        val customerIOMockNorway = CustomerioMock(objetMapper)
        val customerIOMockSweden = CustomerioMock(objetMapper)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NOT_FOUND
        every { memberServiceImpl.getPickedLocale(any()) } returns Locale("en", "gb")

        val router = Router(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to customerIOMockSweden,
            Workspace.NORWAY to customerIOMockNorway
        )

        thrown.expect(RuntimeException::class.java)
        router.updateCustomer("21313", mapOf())
    }
}
