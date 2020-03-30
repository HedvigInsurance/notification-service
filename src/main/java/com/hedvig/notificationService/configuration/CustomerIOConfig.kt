package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.Customerio
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.MemberServiceImpl
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.ProductPricingFacadeImpl
import com.hedvig.notificationService.customerio.Router
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CustomerIOConfig() {

    @Autowired
    lateinit var configuration: ConfigurationProperties

    @Value("\${hedvig.usefakes:false}")
    var useFakes: Boolean = false

    private val okHttp: OkHttpClient = OkHttpClient()

    @Bean("productPricingFacade")
    fun productPricingFacade(productPricingClient: ProductPricingClient) =
        ProductPricingFacadeImpl(productPricingClient)

    @Bean("memberServiceFacade")
    fun memberServiceFacade(memberServiceClient: MemberServiceClient) = MemberServiceImpl(memberServiceClient)

    @Bean
    fun router(
        productPricingFacade: ProductPricingFacade,
        memberServiceImpl: MemberServiceImpl,
        objectMapper: ObjectMapper
    ): Router {

        val clients =
            createClients(objectMapper)

        return Router(
            productPricingFacade,
            memberServiceImpl,
            *clients
        )
    }

    private fun createClients(objectMapper: ObjectMapper): Array<Pair<Workspace, CustomerioClient>> {
        return if (useFakes) {
            val customerioMock = CustomerioMock(objectMapper)
            return arrayOf(
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
            }.toTypedArray()
        }
    }
}
