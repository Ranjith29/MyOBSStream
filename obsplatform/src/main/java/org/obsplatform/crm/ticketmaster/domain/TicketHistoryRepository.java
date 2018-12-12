package org.obsplatform.crm.ticketmaster.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Long>,
JpaSpecificationExecutor<TicketHistory>{

}
