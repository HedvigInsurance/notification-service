package com.hedvig.notificationService.customerio.web

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.events.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/_/customerio/debug")
@Profile("development")
class DebugController(val clients: Map<Workspace, CustomerioClient>) {

    @PostMapping("{memberId}/sendSignEvent")
    fun sendSignEvent(@PathVariable memberId: String) {

        clients[Workspace.NORWAY]?.sendEvent(
            memberId,
            CustomerioEventCreatorImpl().contractCreatedEvent(
                CustomerioState(memberId, null),
                listOf(
                    ContractInfo(AgreementType.NorwegianHomeContent, "IF", LocalDate.parse("2020-05-01")),
                    ContractInfo(AgreementType.NorwegianTravel, "Folksam", null)
                )
            )
        )
    }

    @PostMapping("{memberId}/activationDateUpdatedEvent")
    fun activationDateUpdatedEvent(@PathVariable memberId: String) {

        clients[Workspace.NORWAY]?.sendEvent(
            memberId,
            mapOf<String, Any?>(
                "name" to "ActivationDateUpdatedEvent", "data" to mapOf<String, Any?>(
                    "contractsWithStartDate" to listOf(
                        mapOf(
                            "type" to "innbo",
                            "switcher_company" to "IF",
                            "activation_date" to "2020-05-01"
                        )
                    ),
                    "contractsWithoutStartDate" to listOf(
                        mapOf(
                            "type" to "reise",
                            "switcher_company" to "IF"
                        )
                    )
                )
            )
        )
    }

    @PostMapping("{memberId}/activationDateTodayEvent")
    fun activationDateTodayEvent(@PathVariable memberId: String) {

        clients[Workspace.NORWAY]?.sendEvent(
            memberId,
            mapOf<String, Any?>(
                "name" to "ActivationDateTodayEvent", "data" to mapOf<String, Any?>(
                    "activeToday" to listOf(
                        mapOf(
                            "type" to "innbo",
                            "switcher_company" to "IF"
                        )
                    ),
                    "activeInFuture" to listOf(
                        mapOf(
                            "type" to "reise",
                            "switcher_company" to "IF"
                        )
                    )
                )
            )
        )
    }
}
