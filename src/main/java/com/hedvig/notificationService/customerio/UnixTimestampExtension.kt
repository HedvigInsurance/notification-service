package com.hedvig.notificationService.customerio

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

fun Map<String, Any?>.replaceWithUnixTimestamp(): Map<String, Any?> {
    val mutableMap = this.toMutableMap()
    mutableMap.forEach {
        when (val value = it.value) {
            is LocalDate -> mutableMap[it.key] = value.localDateToUnixTimestamp()
            is LocalDateTime -> mutableMap[it.key] = value.toEpochSecond(ZoneOffset.UTC)
            is Instant -> mutableMap[it.key] = value.atZone(ZoneOffset.UTC).toEpochSecond()
            is String -> {
                try {
                    mutableMap[it.key] = LocalDate.parse(value).localDateToUnixTimestamp()
                } catch (e: Exception) {
                    // no-op
                }
            }
        }
    }

    return mutableMap.toMap()
}

private fun LocalDate.localDateToUnixTimestamp() = this.toEpochSecond(LocalTime.of(0, 0), ZoneOffset.UTC)
