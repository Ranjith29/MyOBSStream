package org.obsplatform.crm.ticketmaster.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketDetailsRepository  extends
	JpaRepository<TicketDetail, Long>,
	JpaSpecificationExecutor<TicketDetail>{
	}


