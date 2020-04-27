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
            (cs.contractCreatedAt IS NOT NULL and cs.contractCreatedAt <= :byTime) OR 
            (cs.underwriterFirstSignAttributesUpdate <= :byTime and cs.sentTmpSignEvent = false)
    """
    )
    override fun shouldUpdate(byTime: Instant): Collection<CustomerioState>
}
