package com.hedvig.common.localization

import com.hedvig.lokalise.client.LokaliseClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Locale

@Component
class LocalizationServiceImpl(
    @Value("\${lokalise.useFakes:false}")
    private val useFakes: Boolean
) : LocalizationService {

    @Autowired
    lateinit var configuration: LokaliseConfigurationProperties

    val client = if (!useFakes) LokaliseClient(configuration.projectId, configuration.apiToken) else null

    override fun getTranslation(key: String, locale: Locale) =
        if (!useFakes) client!!.getTranslation(key, locale) else "lokalise configuration useFakes is set to true"
}