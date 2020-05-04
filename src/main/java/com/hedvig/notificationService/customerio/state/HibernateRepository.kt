package com.hedvig.notificationService.customerio.state

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface HibernateRepository : CustomerIOStateRepository, CrudRepository<CustomerioState, String> {

    @Query(
        """
        FROM CustomerioState cs
        where 
            (cs.contractCreatedTriggerAt IS NOT NULL and cs.contractCreatedTriggerAt <= :byTime) OR 
            (cs.underwriterFirstSignAttributesUpdate <= :byTime and cs.sentTmpSignEvent = false) OR
            (cs.startDateUpdatedTriggerAt IS NOT NULL and cs.startDateUpdatedTriggerAt <= :byTime) OR 
            (cs.activationDateTriggerAt IS NOT NULL and cs.activationDateTriggerAt <= :byTime)
    """
    )
    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState>
}
