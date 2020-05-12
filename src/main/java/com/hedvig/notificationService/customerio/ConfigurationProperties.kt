package com.hedvig.notificationService.customerio

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "hedvig.customerio")
class ConfigurationProperties {

    var useNorwayHack: Boolean = true

    lateinit var workspaces: List<WorkspaceProperties>

    class WorkspaceProperties {
        lateinit var name: Workspace
        lateinit var siteId: String
        lateinit var apiKey: String
    }
}
