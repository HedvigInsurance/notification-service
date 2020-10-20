package com.hedvig.notificationService.utils

private val streetNameRegex = """
    (\w.+)\s+\d
""".trimIndent().toRegex()

fun String.extractStreetName(): String {
    val streetName = streetNameRegex
        .find(this)
        ?.groupValues
        ?.get(1)
        ?.trim()
        ?.replace("""\s+""".toRegex(), " ") ?: this
    return streetName.toTitleCase()
}

fun String.toTitleCase(): String = this
    .split(" ")
    .joinToString(" ") { word -> word[0].toUpperCase() + word.substring(1).toLowerCase() }