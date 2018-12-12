package org.obsplatform.organisation.employee.domain;

import java.util.List;

import org.obsplatform.billing.chargecode.domain.ChargeCodeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
JpaSpecificationExecutor<Employee>{
	
	
}
