package com.hedvig.notificationService.customerio.repository

import com.hedvig.notificationService.customerio.CustomerioState
import java.time.Instant

interface CustomerIOStateRepository {
    fun save(customerioState: CustomerioState)
    fun allMembers(): Set<CustomerioState>
    fun findByMemberId(memberId: String): CustomerioState?
    fun shouldSendTempSignEvent(byTime: Instant): Collection<CustomerioState>
}
