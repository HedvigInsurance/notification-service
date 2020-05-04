package com.hedvig.notificationService.customerio.state

import java.time.Instant
import java.time.ZoneId

class InMemoryCustomerIOStateRepository(var data: Map<String, CustomerioState> = mapOf()) :
    CustomerIOStateRepository {
    override fun save(customerioState: CustomerioState) {
        data = data.plus(customerioState.memberId to customerioState)
    }

    override fun findByMemberId(memberId: String): CustomerioState? {
        return data[memberId]
    }

    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState> {
        return data.values.filter {
            (it.underwriterFirstSignAttributesUpdate != null &&
                it.underwriterFirstSignAttributesUpdate <= byTime &&
                !it.sentTmpSignEvent) ||
                (it.contractCreatedTriggerAt != null && it.contractCreatedTriggerAt <= byTime) ||
                (it.startDateUpdatedTriggerAt != null && it.startDateUpdatedTriggerAt <= byTime) ||
                (it.activationDateTriggerAt != null && it.activationDateTriggerAt <=
                    byTime.atZone(ZoneId.of("Europe/Stockholm")).toLocalDate())
        }
    }
}
