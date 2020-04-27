package com.hedvig.notificationService.customerio.state

import java.time.Instant

interface CustomerIOStateRepository {
    fun save(customerioState: CustomerioState)
    fun findByMemberId(memberId: String): CustomerioState?
    fun shouldSendTempSignEvent(byTime: Instant): Collection<CustomerioState>
    fun shouldSendContractCreatedEvents(byTime: Instant): Collection<CustomerioState>
}
