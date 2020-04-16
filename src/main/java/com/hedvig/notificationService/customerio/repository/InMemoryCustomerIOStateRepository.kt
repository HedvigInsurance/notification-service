package com.hedvig.notificationService.customerio.repository

import com.hedvig.notificationService.customerio.CustomerioState
import java.time.Instant

class InMemoryCustomerIOStateRepository(var data: Map<String, CustomerioState> = mapOf()) :
    CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {
        data = data.plus(customerioState.memberId to customerioState)
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        return data[memberId]
    }

    override fun shouldSendTempSignEvent(byTime: Instant): Collection<CustomerioState> {
        return data.values.filter { it.underwriterFirstSignAttributesUpdate <= byTime && !it.sentTmpSignEvent }
    }
}
