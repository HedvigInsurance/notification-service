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
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
class UnixTimestampExtensionTest {
    
    private val utc = ZoneId.of("UTC")

    @Test
    fun `given LocalDate of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to LocalDate.of(2020, 8, 17))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597622400L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given LocalDateTime of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to LocalDateTime.of(2020, 8, 17, 20, 17))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597695420L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given Instant of sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to Instant.parse("2020-08-17T20:17:00.00Z"))

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597695420L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given String of 2020-08-17 sign date is replace with unix timestamp`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to "2020-08-17")

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)
        val expectedMap = map.toMutableMap().apply { this["signed_at"] = 1597622400L }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given multiple dates and replace all`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to "2020-08-17", "terminated_at" to "2020-08-19", "recovered_at" to "2020-08-18")

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)
        val expectedMap = map.toMutableMap().apply {
            this["signed_at"] = 1597622400L
            this["terminated_at"] = 1597795200L
            this["recovered_at"] = 1597708800L
        }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given map in map with local date time is replaced`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "signed_at" to LocalDateTime.of(2020, 8, 17, 20, 17))

        val dataMap: Map<String, Any?> = mapOf("data" to map)

        val mapWithUnixTimestamp = dataMap.replaceWithUnixTimestamp(utc)
        val expectedMap = mapOf(
            "data" to mapOf(
                "member_id" to "1234",
                "signed_at" to 1597695420L
            )
        )

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given map in map of String to Int does not blow up`() {
        val map: Map<String, Int> = mapOf("member_id" to 1234)

        val dataMap: Map<String, Any?> = mapOf("data" to map)

        val mapWithUnixTimestamp = dataMap.replaceWithUnixTimestamp(utc)
        val expectedMap = mapOf(
            "data" to mapOf(
                "member_id" to 1234
            )
        )

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given map in map of Int to Int does not blow up`() {
        val map: Map<Int, Int> = mapOf(1234 to 1234)

        val dataMap: Map<String, Any?> = mapOf("data" to map)

        val mapWithUnixTimestamp = dataMap.replaceWithUnixTimestamp(utc)
        val expectedMap = mapOf(
            "data" to mapOf(
                1234 to 1234
            )
        )

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `given only string and int does nothing`() {
        val map: Map<String, Any?> = mapOf("member_id" to "1234", "member_age" to 35)

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(utc)

        assertThat(mapWithUnixTimestamp).isEqualTo(map)
    }

    @Test
    fun `test stockholm time zones to utc`() {
        val map: Map<String, Any?> = mapOf(
            "dateSummerTime" to LocalDate.of(2020, 8, 17),
            "dateWinterTime_1" to LocalDateTime.of(2020, 11, 10, 0, 0),
            "dateWinterTime_2" to "2020-11-10",
            "utcDate" to Instant.parse("2020-11-10T00:00:00Z")
        )

        val dateSummerTime = 1597615200L //UTC 2020-08-16 22:00 Stockholm time 2020-08-17 00:00
        val dateWinterTime = 1604962800L //UTC 2020-11-09 23:00 Stockholm time 2020-11-10 00:00
        val utcDate = 1604966400L //UTC 2020-11-10 00:00

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(ZoneId.of("Europe/Stockholm"))
        val expectedMap = map.toMutableMap().apply {
            this["dateSummerTime"] = dateSummerTime
            this["dateWinterTime_1"] = dateWinterTime
            this["dateWinterTime_2"] = dateWinterTime
            this["utcDate"] = utcDate
        }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }

    @Test
    fun `test new york time zones to utc`() {
        val map: Map<String, Any?> = mapOf(
            "dateSummerTime" to LocalDate.of(2020, 8, 17),
            "dateWinterTime_1" to LocalDateTime.of(2020, 11, 10, 0, 0),
            "dateWinterTime_2" to "2020-11-10"
        )

        val dateSummerTime = 1597636800L //UTC 2020-08-17 04:00 NY 2020-08-17 00:00
        val dateWinterTime = 1604984400L //UTC 2020-11-10 05:00 NY 2020-11-10 00:00

        val mapWithUnixTimestamp = map.replaceWithUnixTimestamp(ZoneId.of("America/New_York"))
        val expectedMap = map.toMutableMap().apply {
            this["dateSummerTime"] = dateSummerTime
            this["dateWinterTime_1"] = dateWinterTime
            this["dateWinterTime_2"] = dateWinterTime
        }.toMap()

        assertThat(mapWithUnixTimestamp).isEqualTo(expectedMap)
    }
}
