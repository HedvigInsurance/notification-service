package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.Customerio
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioRemoveIdempotenceHashesJob
import com.hedvig.notificationService.customerio.CustomerioUpdateScheduler
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoaderImpl
import com.hedvig.notificationService.customerio.hedvigfacades.FakeContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.QuartzMigrator
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import com.hedvig.notificationService.serviceIntegration.underwriter.UnderwriterClient
import okhttp3.OkHttpClient
import org.quartz.JobBuilder
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.Instant
import javax.annotation.PostConstruct

@Configuration
@EnableScheduling
class CustomerIOConfig(
    private val scheduler: Scheduler
) {

    @Autowired
    lateinit var configuration: ConfigurationProperties

    @Value("\${hedvig.usefakes:false}")
    var useFakes: Boolean = false

    private val okHttp: OkHttpClient = OkHttpClient()

    @EventListener
    fun onContextRefereshedEvent(event: ContextRefreshedEvent) {
        val quartzMigrator = event.applicationContext.getBean(QuartzMigrator::class.java)
        quartzMigrator.migrate(Instant.now())
    }

    @Bean()
    fun productPricingFacade(productPricingClient: ProductPricingClient, underwriterClient: UnderwriterClient) =
        if (useFakes) {
            FakeContractLoader()
        } else {
            ContractLoaderImpl(
                productPricingClient,
                underwriterClient
            )
        }

    @Bean()
    fun memberServiceFacade(memberServiceClient: MemberServiceClient) =
        MemberServiceImpl(memberServiceClient)

    @Bean
    fun createClients(objectMapper: ObjectMapper): Map<Workspace, CustomerioClient> {
        return if (useFakes) {
            val customerioMock = CustomerioMock(objectMapper)
            return mapOf(
                Workspace.SWEDEN to customerioMock,
                Workspace.NORWAY to customerioMock
            )
        } else {
            this.configuration.workspaces.map {
                it.name to Customerio(
                    it.siteId,
                    it.apiKey,
                    objectMapper,
                    okHttp
                )
            }.toMap()
        }
    }

    @PostConstruct
    fun scheduleCustomerio() {
        val job = JobBuilder.newJob()
            .ofType(CustomerioUpdateScheduler::class.java)
            .withIdentity("Customerio_update_scheduler")
            .withDescription("Customer.io update scheduler")
            .requestRecovery(true)
            .build()

        val trigger = TriggerBuilder
            .newTrigger()
            .forJob(job)
            .withIdentity("Customerio_update_scheduler_trigger")
            .withSchedule(simpleSchedule().repeatForever().withIntervalInSeconds(30))
            .startNow()
            .build()

        if (!scheduler.checkExists(job.key) && !scheduler.checkExists(trigger.key)) {
            scheduler.scheduleJob(job, trigger)
        }
        scheduler.start()
    }

    @PostConstruct
    fun scheduleRemoveHashes() {
        val job = JobBuilder.newJob()
            .ofType(CustomerioRemoveIdempotenceHashesJob::class.java)
            .withIdentity("Customerio_remove_idempotence_hashes")
            .withDescription("Customer.io remove hashes scheduler")
            .requestRecovery(true)
            .build()

        val trigger = TriggerBuilder
            .newTrigger()
            .forJob(job)
            .withIdentity("Customerio_remove_idempotence_hashes_trigger")
            .withSchedule(simpleSchedule().repeatForever().withIntervalInHours(1))
            .startNow()
            .build()

        if (!scheduler.checkExists(job.key) && !scheduler.checkExists(trigger.key)) {
            scheduler.scheduleJob(job, trigger)
        }
    }
}
