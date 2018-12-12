package org.obsplatform.organisation.department.service;

import java.util.List;

import org.obsplatform.organisation.department.data.DepartmentData;

public interface DepartmentReadPlatformService {

	DepartmentData retrieveDepartmentData(Long deptId);
	
	List<DepartmentData> retrieveAllDepartmentData();

	DepartmentData retrieveDepartmentId(String deptName);
}
