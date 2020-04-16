package com.hedvig.notificationService.customerio

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CreateTmpSignEventTest {

    @Test
    fun `Inbo contract`() {

        val sut = CustomerioEventCreatorImpl()
        
        val eventData = sut.createTmpSignedInsuranceEvent()

        assertThat(eventData["name"]).isEqualTo("TmpSignedInsuranceEvent")
    }
}
