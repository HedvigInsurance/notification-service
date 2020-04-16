package com.hedvig.notificationService.customerio

import java.time.Instant

class InMemoryCustomerIOStateRepository(var data: Map<String, CustomerioState> = mapOf()) : CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {
        data = data.plus(customerioState.memberId to customerioState)
    }

    override fun allMembers(): Set<CustomerioState> {
        return data.values.toSet()
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        return data[memberId]
    }

    override fun shouldSendTempSignEvent(byTime: Instant): Collection<CustomerioState> {
        return data.values.filter { it.underwriterSignAttributesStarted <= byTime && !it.sentTmpSignEvent }
    }
}
