package com.hedvig.notificationService.customerio.web

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.Workspace
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/customerio/debug")
@Profile("development")
class DebugController(val clients: Map<Workspace, CustomerioClient>) {

    @PostMapping("{memberId}/sendSignEvent")
    fun sendSignEvent(@PathVariable memberId: String) {

        clients[Workspace.NORWAY]?.sendEvent(
            memberId,
            mapOf<String, Any?>(
                "name" to "TestSignEvent", "data" to mapOf<String, Any?>(
                    "contracts" to listOf(
                        mapOf(
                            "type" to "innbo",
                            "switcher_company" to "IF"
                        ),
                        mapOf(
                            "type" to "reise",
                            "activation_date" to "2020-05-01"
                        )
                    )
                )
            )
        )
    }
}
