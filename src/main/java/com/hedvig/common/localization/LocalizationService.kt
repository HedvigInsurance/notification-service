package com.hedvig.common.localization

import java.util.Locale

interface LocalizationService {
    fun getTranslation(key: String, locale: Locale): String?
}

