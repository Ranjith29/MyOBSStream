package org.obsplatform.organisation.employee.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.employee.data.EmployeeData;

public interface EmployeeReadPlatformService {
	
	EmployeeData retrieveEmployeeData(Long employeeId);

	List<EmployeeData> retrieveAllEmployeeData();
	
	Page<EmployeeData> retrieveAllEmployeeData(SearchSqlQuery searchEmployee);
	
	List<EmployeeData> retrieveAllEmployeeDataByDeptId(Long departmentId);

}
