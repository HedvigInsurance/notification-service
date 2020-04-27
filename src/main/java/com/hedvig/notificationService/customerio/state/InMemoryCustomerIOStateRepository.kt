package com.hedvig.notificationService.customerio.state

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
        return data.values.filter {
            it.underwriterFirstSignAttributesUpdate != null &&
                it.underwriterFirstSignAttributesUpdate <= byTime &&
                !it.sentTmpSignEvent
        }
    }

    override fun shouldSendContractCreatedEvents(byTime: Instant): Collection<CustomerioState> {
        return data.values.filter {
            it.contractCreatedAt != null && it.contractCreatedAt <= byTime
        }
    }
}
