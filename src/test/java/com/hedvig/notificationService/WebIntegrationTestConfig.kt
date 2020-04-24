package com.hedvig.notificationService

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.FakeProductPricingFacade
import com.hedvig.notificationService.customerio.MemberServiceImpl
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.events.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.serviceIntegration.memberService.FakeMemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@TestConfiguration
class WebIntegrationTestConfig {

    @Bean
    fun customerIO(objectMapper: ObjectMapper): CustomerioMock {
        return CustomerioMock(objectMapper)
    }

    fun productPricingClientTest(): ProductPricingFacade = FakeProductPricingFacade()

    fun memberServiceClientTest(): MemberServiceClient = FakeMemberServiceClient()

    @Bean
    @Primary
    fun customerioServiceTest(customerioMock: CustomerioClient): CustomerioService {
        val productPricingFacade = productPricingClientTest()
        return CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                MemberServiceImpl(memberServiceClientTest())
            ),
            InMemoryCustomerIOStateRepository(),
            CustomerioEventCreatorImpl(productPricingFacade),
            mapOf(
                Workspace.SWEDEN to customerioMock,
                Workspace.NORWAY to customerioMock
            )
        )
    }
}
