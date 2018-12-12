package org.obsplatform.organisation.ticketassignrule.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TicketAssignRuleJpaRepository extends JpaRepository<TicketAssignRule, Long>,JpaSpecificationExecutor<TicketAssignRule> {

}
