package com.hedvig.notificationService.customerio.repository

import com.hedvig.notificationService.customerio.CustomerioState
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface HibernateRepository : CustomerIOStateRepository, CrudRepository<CustomerioState, String> {

    @Query(
        """
        FROM CustomerioState cs
        where cs.underwriterFirstSignAttributesUpdate <= :byTime and cs.sentTmpSignEvent = false
        
    """
    )
    override fun shouldSendTempSignEvent(@Param("byTime") byTime: Instant): Collection<CustomerioState>
}
