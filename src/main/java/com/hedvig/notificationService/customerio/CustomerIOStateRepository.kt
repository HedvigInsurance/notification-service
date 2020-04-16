package com.hedvig.notificationService.customerio

interface CustomerIOStateRepository {
    fun save(customerioState: CustomerioState)
    fun allMembers(): Set<CustomerioState>
    fun findByMemberId(memberId: String): CustomerioState?
}
