package com.hedvig.notificationService.utils

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.Test

internal class TextUtilsTest {
    @Test
    fun `properly extracts street name from regular street`() {
        val street = "Valhallavägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly extracts street name from street with multiple spaces`() {
        val street = "Valhallavägen    117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly trims street name from street`() {
        val street = "    Valhallavägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly handles street with spaces`() {
        val street = "Valhalla Vägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhalla Vägen")
    }

    @Test
    fun `properly handles street with spaces and trims spaces in between`() {
        val street = "Valhalla        Vägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhalla Vägen")
    }

    @Test
    fun `properly returns street for address without number`() {
        val street = "Valhallavägen"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly returns titlecase for lowercase address`() {
        val street = "valhallavägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly returns titlecase for uppercase address`() {
        val street = "VALHALLAVÄGEN 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhallavägen")
    }

    @Test
    fun `properly returns titlecase for lowercase address with spaces`() {
        val street = "valhalla vägen 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhalla Vägen")
    }

    @Test
    fun `properly returns titlecase for uppercase address with spaces`() {
        val street = "VALHALLA VÄGEN 117k"
        val streetName = street.extractStreetName()
        assertThat(streetName).isEqualTo("Valhalla Vägen")
    }
}
