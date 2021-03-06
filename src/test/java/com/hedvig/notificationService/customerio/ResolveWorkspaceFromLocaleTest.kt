package com.hedvig.notificationService.customerio

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.Locale

class ResolveWorkspaceFromLocaleTest {

    @Test
    fun `return sweden give se as country`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("sv", "se"))).isEqualTo(
            Workspace.SWEDEN
        )
    }

    @Test
    fun `return norway market given no as country`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("sv", "no"))).isEqualTo(
            Workspace.NORWAY
        )
    }

    @Test
    fun `return denmark market given dk as country`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("en", "dk"))).isEqualTo(
            Workspace.DENMARK
        )
    }

    @Test
    fun `return not found on unsupported locale`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("nb", "us"))).isEqualTo(
            Workspace.NOT_FOUND
        )
    }

    @Test
    fun `return not found on weird locale`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("nb", "xx"))).isEqualTo(
            Workspace.NOT_FOUND
        )
    }

    @Test
    fun `return not found on empty country in locale`() {
        assertThat(Workspace.getWorkspaceFromLocale(Locale("nb", ""))).isEqualTo(
            Workspace.NOT_FOUND
        )
    }
}
