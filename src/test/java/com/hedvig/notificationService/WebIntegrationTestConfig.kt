package com.hedvig.notificationService

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.FakeProductPricingFacade
import com.hedvig.notificationService.customerio.MemberServiceImpl
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.Router
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.serviceIntegration.memberService.FakeMemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Configuration
class WebIntegrationTestConfig {

    @Bean
    fun customerIO(objectMapper: ObjectMapper): CustomerioMock {
        return CustomerioMock(objectMapper)
    }

    @Bean
    fun productPricingClient(): ProductPricingFacade = FakeProductPricingFacade()

    @Bean
    fun memberServiceClient(): MemberServiceClient = FakeMemberServiceClient()

    @Bean
    fun customerioRouter(customerioMock: CustomerioClient): Router {
        return Router(
            productPricingClient(),
            MemberServiceImpl(memberServiceClient()),
            Workspace.SWEDEN to customerioMock,
            Workspace.NORWAY to customerioMock
        )
    }
}
