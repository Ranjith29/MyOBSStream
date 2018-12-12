package org.obsplatform.organisation.department.data;

import java.util.Collection;

import org.obsplatform.organisation.office.data.OfficeData;

public class DepartmentData {

	private Long id;
	private String deptName;
	private String deptDescription;
	private Long officeId;
	private Collection<OfficeData> officeData;
	private String officeName;
	private String allocatedAsPrimary;

	public DepartmentData(final Long deptId, final String deptName,final String deptDescription, final Long officeId, 
			final String officeName, final String allocatedAsPrimary) {
		this.id = deptId;
		this.deptName = deptName;
		this.deptDescription = deptDescription;
		this.officeId = officeId;
		this.officeName = officeName;
		this.allocatedAsPrimary = allocatedAsPrimary;
	}

	public DepartmentData(Collection<OfficeData> officeData) {
		this.officeData = officeData;
	}

	public Long getId() {
		return id;
	}

	public String getDeptName() {
		return deptName;
	}

	public String getDeptDescription() {
		return deptDescription;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public Collection<OfficeData> getOfficeData() {
		return officeData;
	}

	public String getOfficeName() {
		return officeName;
	}

	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}

	public void setOfficeData(Collection<OfficeData> officeData) {
		this.officeData = officeData;
	}

	public String getAllocatedAsPrimary() {
		return allocatedAsPrimary;
	}
}
