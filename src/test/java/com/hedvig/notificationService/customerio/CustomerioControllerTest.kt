package com.hedvig.notificationService.customerio

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.web.CustomerioController
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.springframework.web.server.ResponseStatusException

class CustomerioControllerTest {

    @get:Rule
    val thrown = ExpectedException.none()

    @MockK
    lateinit var customerioService: CustomerioService

    private val objectMapper = ObjectMapper()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `return 500 on locale not found`() {
        every {
            customerioService.updateCustomerAttributes(
                any(),
                any(),
                any()
            )
        } throws WorkspaceNotFound("")

        val controller =
            CustomerioController(
                customerioService,
                objectMapper
            )

        thrown.expect(ResponseStatusException::class.java)
        controller.post("someMemberID", objectMapper.createObjectNode())
    }

    @Test
    fun `delete returns 500 on workspace not found`() {
        every { customerioService.deleteCustomer(any()) } throws WorkspaceNotFound("")

        val controller =
            CustomerioController(
                customerioService,
                objectMapper
            )

        thrown.expect(ResponseStatusException::class.java)
        controller.delete("someMemberID")
    }
}
