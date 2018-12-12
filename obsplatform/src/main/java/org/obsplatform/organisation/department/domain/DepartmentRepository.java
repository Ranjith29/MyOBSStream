package org.obsplatform.organisation.department.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepartmentRepository extends JpaRepository<Department, Long>,JpaSpecificationExecutor<Department> {

}
