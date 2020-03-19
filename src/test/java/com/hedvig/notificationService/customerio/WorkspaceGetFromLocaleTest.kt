package com.hedvig.notificationService.customerio

import java.util.Locale
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class WorkspaceGetFromLocaleTest {

    @Test
    fun getMarketFromLocaleTest() {
        assertThat(getWorkspaceFromLocale(Locale("sv", "se"))).isEqualTo(
            Workspace.SWEDEN)
    }

    @Test
    fun `return norway market given no as country`() {
        assertThat(getWorkspaceFromLocale(Locale("sv", "no"))).isEqualTo(
            Workspace.NORWAY)
    }

    @Test
    fun `throw exception on unsupported locale`() {
        assertThat(getWorkspaceFromLocale(Locale("nb", "us"))).isEqualTo(
            Workspace.NOT_FOUND)
    }
}
