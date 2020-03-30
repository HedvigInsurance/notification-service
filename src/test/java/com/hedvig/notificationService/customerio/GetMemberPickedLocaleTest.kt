package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.PickedLocale
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.springframework.http.ResponseEntity
import java.io.Serializable
import java.util.Locale

@RunWith(Parameterized::class)
class GetMemberPickedLocaleTest(val pickedLocale: String, val parsedLocale: Locale) {

    companion object {
        @Parameterized.Parameters
        @JvmStatic
        fun data(): List<Array<Serializable>> {
            return listOf(
                arrayOf("sv_SE", Locale("sv", "SE")),
                arrayOf("en_SE", Locale("en", "SE")),
                arrayOf("nb_NO", Locale("nb", "NO")),
                arrayOf("en_NO", Locale("en", "NO"))
            )
        }
    }

    @Test
    fun equals() {
        val memberService = mockk<MemberServiceClient>()
        every { memberService.pickedLocale(any()) } returns ResponseEntity.ok(PickedLocale(pickedLocale))

        val cut = MemberServiceImpl(memberService)
        assertThat(cut.getPickedLocale("1231")).isEqualTo(parsedLocale)
    }
}
