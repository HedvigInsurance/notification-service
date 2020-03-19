package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class RouterContructionTest {

    @get:Rule
    var exceptionRule: ExpectedException = ExpectedException.none()

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    @MockK
    lateinit var customerioClient: CustomerioClient

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `Throw if no markets are passed in the contructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        Router(productPricingFacade, memberServiceImpl)
    }

    @Test
    fun `Throw if not all markets are passed in the contructor`() {
        exceptionRule.expect(IllegalArgumentException::class.java)
        Router(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to customerioClient
        )
    }

    @Test
    fun `Do not throw when all markets are passed in the contructor`() {
        Router(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to customerioClient,
            Workspace.NORWAY to customerioClient
        )
    }
}
