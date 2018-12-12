package org.obsplatform.organisation.department.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;

@Entity
@Table(name = "b_office_department")
public class Department {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(name="department_name")
	private String deptName;
	
	@Column(name="department_description")
	private String deptDescription;
	
	@Column(name="office_id")
	private Long officeId;
	
	@Column(name="is_deleted")
	private String deleted = "N";
	
	@Column(name="is_allocated")
	private String allocated = "No";
	
	public Department(){
		
	}
	
	public Department(final String deptName,final String deptDescription,final Long officeId){
		this.deptName = deptName;
		this.deptDescription = deptDescription;
		this.officeId = officeId;
	}
	
    public static Department fromJson(final JsonCommand command) {
		
		final String deptName = command.stringValueOfParameterNamed("deptname");
		final String deptDescription=command.stringValueOfParameterNamed("deptdescription");
		final Long officeId=command.longValueOfParameterNamed("officeid");
		return new Department(deptName, deptDescription, officeId);
	}
    
    public Map<String, Object> update(final JsonCommand command) {

		  final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		  final String departmentParamName = "deptname";
	        if (command.isChangeInStringParameterNamed(departmentParamName, this.deptName)) {
	            final String newValue = command.stringValueOfParameterNamed(departmentParamName);
	            actualChanges.put(departmentParamName, newValue);
	            this.deptName = StringUtils.defaultIfEmpty(newValue, null);
	        }

	        final String departmentDescription = "deptdescription";
	        if (command.isChangeInStringParameterNamed(departmentDescription, this.deptDescription)) {
	            final String newValue = command.stringValueOfParameterNamed(departmentDescription);
	            actualChanges.put(departmentDescription, newValue);
	            this.deptDescription = StringUtils.defaultIfEmpty(newValue, null);
	        }
			if (command.isChangeInLongParameterNamed("officeid",this.officeId)) {
				final Long newValue = command.longValueOfParameterNamed("officeid");
				actualChanges.put("officeid", newValue);
				this.officeId = newValue;
			}

      return actualChanges;

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
	
	public void delete() {
		this.deleted = "Y";
	}
	
	public void allocated() {
		this.allocated = "Yes";
	}

	public String getAllocated() {
		return allocated;
	}

	public void setAllocated(String allocated) {
		this.allocated = allocated;
	}

	
}
