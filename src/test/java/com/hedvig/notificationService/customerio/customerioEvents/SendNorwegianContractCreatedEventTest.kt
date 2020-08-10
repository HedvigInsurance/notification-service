package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractCreatedJob
import com.hedvig.notificationService.customerio.customerioEvents.jobs.makeJobExecutionContext
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.serviceIntegration.productPricing.FeignExceptionForTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.quartz.JobDataMap
import org.quartz.Scheduler
import java.time.Instant

class SendNorwegianContractCreatedEventTest {

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK
    lateinit var contractLoader: ContractLoader

    val repo = InMemoryCustomerIOStateRepository()

    @MockK(relaxed = true)
    lateinit var noClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var seClient: CustomerioClient

    lateinit var customerioService: CustomerioService
    lateinit var contractCreatedJob: ContractCreatedJob

    val scheduler: Scheduler = mockk(relaxed = true)

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        customerioService = CustomerioService(
            workspaceSelector,
            repo,
            mapOf(
                Workspace.NORWAY to noClient,
                Workspace.SWEDEN to seClient
            ),
            ConfigurationProperties()
        )
        contractCreatedJob = ContractCreatedJob(
            contractLoader,
            CustomerioEventCreatorImpl(),
            customerioService,
            repo
        )
    }

    @Test
    fun sendContractCreatedEvent() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { contractLoader.getContractInfoForMember(any()) } returns
            listOf(
                makeContractInfo(
                    AgreementType.NorwegianHomeContent,
                    switcherCompany = null,
                    startDate = null,
                    signSource = "IOS",
                    partnerCode = "HEDVIG"
                )
            )

        val jobData = JobDataMap()
        jobData["memberId"] = "someMemberId"

        contractCreatedJob.execute(makeJobExecutionContext(scheduler, contractCreatedJob, jobData))

        val slot = slot<Map<String, Any?>>()
        verify { noClient.sendEvent(any(), capture(slot)) }

        assertThat(slot.captured["name"]).isEqualTo("NorwegianContractCreatedEvent")
        assertThat(slot.captured["data"] as Map<String, Any>).contains("is_signed_innbo", true)
    }

    @Test
    fun sendContractCreatedEventToSwedishMember() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        every { contractLoader.getContractInfoForMember(any()) } returns
            listOf(
                makeContractInfo(
                    AgreementType.NorwegianHomeContent,
                    switcherCompany = null,
                    startDate = null,
                    signSource = "IOS",
                    partnerCode = "HEDVIG"
                )
            )

        val jobData = JobDataMap()
        jobData["memberId"] = "someMemberId"

        contractCreatedJob.execute(makeJobExecutionContext(scheduler, contractCreatedJob, jobData))

        val slot = slot<Map<String, Any?>>()
        verify { seClient.sendEvent(any(), any()) }
    }

    @Test
    fun `exception during sending does not update state`() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { noClient.sendEvent(any(), any()) } throws FeignExceptionForTest(500)

        val jobData = JobDataMap()
        jobData["memberId"] = "someMemberId"

        contractCreatedJob.execute(makeJobExecutionContext(scheduler, contractCreatedJob, jobData))

        assertThat(repo.data["someMemberId"]?.contractCreatedTriggerAt).isEqualTo(startTime)
    }
}
