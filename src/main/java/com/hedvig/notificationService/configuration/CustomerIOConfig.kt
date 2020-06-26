package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.Customerio
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoaderImpl
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import com.hedvig.notificationService.serviceIntegration.underwriter.UnderwriterClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
class CustomerIOConfig() {

    @Autowired
    lateinit var configuration: ConfigurationProperties

    @Value("\${hedvig.usefakes:false}")
    var useFakes: Boolean = false

    private val okHttp: OkHttpClient = OkHttpClient()

    @Bean()
    fun productPricingFacade(productPricingClient: ProductPricingClient, underwriterClient: UnderwriterClient) =
        ContractLoaderImpl(
            productPricingClient,
            underwriterClient
        )

    @Bean()
    fun memberServiceFacade(memberServiceClient: MemberServiceClient) =
        MemberServiceImpl(memberServiceClient)

    @Bean
    fun customerioService(
        contractLoader: ContractLoader,
        memberServiceImpl: MemberServiceImpl,
        objectMapper: ObjectMapper,
        repo: CustomerIOStateRepository,
        clients: Map<Workspace, CustomerioClient>,
        workspaceSelector: WorkspaceSelector
    ): CustomerioService {

        return CustomerioService(
            workspaceSelector,
            repo,
            CustomerioEventCreatorImpl(),
            clients,
            contractLoader,
            configuration.useNorwayHack
        )
    }

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
}
