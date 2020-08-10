package com.hedvig.notificationService.customerio.state

import java.time.Instant
import java.util.stream.Stream

interface CustomerIOStateRepository {
    fun save(customerioState: CustomerioState)
    fun findByMemberId(memberId: String): CustomerioState?
    fun shouldUpdate(byTime: Instant): Stream<CustomerioState>
    fun statesWithTriggers(): Stream<CustomerioState>
}
