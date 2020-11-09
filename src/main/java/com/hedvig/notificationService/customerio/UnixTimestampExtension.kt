package com.hedvig.notificationService.customerio

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

fun Map<String, Any?>.replaceWithUnixTimestamp(zoneId: ZoneId): Map<String, Any?> {
    val mutableMap = this.toMutableMap()
    mutableMap.forEach {
        when (val value = it.value) {
            is LocalDate -> mutableMap[it.key] = value.localDateToUnixTimestamp(zoneId)
            is LocalDateTime -> mutableMap[it.key] = value.toEpochSecond(zoneId.rules.getOffset(value))
            is Instant -> mutableMap[it.key] = value.atZone(ZoneOffset.UTC).toEpochSecond()
            is String -> {
                try {
                    mutableMap[it.key] = LocalDate.parse(value).localDateToUnixTimestamp(zoneId)
                } catch (e: Exception) {
                    // no-op
                }
            }
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                mutableMap[it.key] = (value as Map<String, Any?>?)?.replaceWithUnixTimestamp(zoneId)
            }
        }
    }

    return mutableMap.toMap()
}

private fun LocalDate.localDateToUnixTimestamp(zoneId: ZoneId): Long {
    val localDateTime = this.atTime(0,0)
    return localDateTime.toEpochSecond(zoneId.rules.getOffset(localDateTime))
}
