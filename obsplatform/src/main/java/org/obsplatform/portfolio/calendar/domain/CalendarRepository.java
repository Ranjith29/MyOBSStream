package org.obsplatform.portfolio.calendar.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CalendarRepository extends JpaRepository<Calendar, Long>, JpaSpecificationExecutor<Calendar> {

}
