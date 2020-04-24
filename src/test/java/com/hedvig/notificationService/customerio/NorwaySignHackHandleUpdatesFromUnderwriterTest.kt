package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreator
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NorwaySignHackHandleUpdatesFromUnderwriterTest {

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK(relaxed = true)
    lateinit var seCustomerioClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var noCustomerIoClient: CustomerioClient

    @MockK
    lateinit var eventCreator: CustomerioEventCreator

    @MockK
    lateinit var sut: CustomerioService

    lateinit var repository: InMemoryCustomerIOStateRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        repository =
            InMemoryCustomerIOStateRepository(
                mapOf()
            )

        sut = CustomerioService(
            workspaceSelector,
            repository,
            eventCreator,
            mapOf(
                Workspace.SWEDEN to seCustomerioClient,
                Workspace.NORWAY to noCustomerIoClient
            )
        )
    }

    @Test
    fun norwegianMemberSignAttributeDoesNotUpdateCustomerIo() {

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        sut.updateCustomerAttributes(
            "1338", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), Instant.parse("2020-04-15T14:53:40.550493Z")
        )

        verify(inverse = true) { noCustomerIoClient.sendEvent("1338", any()) }
    }

    @Test
    fun swedishSignAttributesDoesUpdateCustomerIo() {

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        sut.updateCustomerAttributes(
            "1337", signFromUnderwriterMap(), Instant.parse("2020-04-15T14:53:40.550493Z")
        )

        verify { seCustomerioClient.updateCustomer("1337", any()) }
    }

    @Test
    fun norwegianSignAttributesUpdatesRepositoryWithTime() {
        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val updateInstant = Instant.parse("2020-04-15T14:53:40.550493Z")
        sut.updateCustomerAttributes("randomId", signFromUnderwriterMap(), updateInstant)

        assertThat(repository.data["randomId"]?.underwriterFirstSignAttributesUpdate).isEqualTo(updateInstant)
    }

    @Test
    fun norwegianSecondSignAttributesDoesNotUpdateRepositoryWithTime() {
        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val updateInstant = Instant.parse("2020-04-15T14:53:40.550493Z")
        sut.updateCustomerAttributes("randomId", signFromUnderwriterMap(), updateInstant)
        sut.updateCustomerAttributes("randomId", signFromUnderwriterMap(), updateInstant.plusMillis(1000))

        assertThat(repository.data["randomId"]?.underwriterFirstSignAttributesUpdate).isEqualTo(updateInstant)
    }

    @Test
    fun updateThatIsNotSignUpdateForwardsToCustomerio() {
        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        sut.updateCustomerAttributes("someID", mapOf("first_name" to "Test", "last_name" to "Ersson"))

        verify { noCustomerIoClient.updateCustomer("someID", any()) }
    }
}

private fun signFromUnderwriterMap(): Map<String, Any?> {
    return mapOf<String, Any?>(
        "partner_code" to "campaigncode",
        "sign_source" to "RAPIO",
        "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
        "switcher_company" to null,
        "is_switcher" to false
    )
}
