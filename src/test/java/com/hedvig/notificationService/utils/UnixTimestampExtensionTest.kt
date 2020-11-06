package com.hedvig.notificationService.utils

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.replaceWithUnixTimestamp
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class UnixTimestampExtensionTest {

    @Test
    fun `given LocalDate of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to LocalDate.of(2020, 8, 17))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597622400L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given LocalDateTime of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to LocalDateTime.of(2020, 8, 17, 20, 17))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597695420L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given Instant of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to Instant.parse("2020-08-17T20:17:00.00Z"))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597695420L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given String of 2020-08-17 sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to "2020-08-17")

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597622400L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given multiple dates and replace all`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to "2020-08-17", "terminated_at" to "2020-08-19", "recovered_at" to "2020-08-18")

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()
        val expectedMap = map.toMutableMap().apply {
            this["signed_at"] = 1597622400L
            this["terminated_at"] = 1597795200L
            this["recovered_at"] = 1597708800L
        }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given only string and int dose nothing`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "member_age" to 35)

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp()

        assertThat(mapWithUnixTimestamp).isEqualTo(map)
    }
}