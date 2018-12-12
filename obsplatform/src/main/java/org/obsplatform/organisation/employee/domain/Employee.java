package org.obsplatform.organisation.employee.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.jpa.domain.AbstractPersistable;

@SuppressWarnings("serial")
@Entity
@Table(name = "b_office_employee")
public class Employee extends AbstractPersistable<Long> {

	@Column(name = "employee_name")
	private String name;

	@Column(name = "employee_loginname")
	private String loginname;

	@Column(name = "employee_password")
	private String password;

	@Column(name = "employee_phone")
	private String phone;

	@Column(name = "employee_email")
	private String email;

	@Column(name = "department_id", nullable = false)
	private Long departmentId;

	@Column(name = "employee_isprimary")
	private boolean isprimary;

	@Column(name = "is_deleted")
	private String deleted = "n";

	@Column(name = "user_id")
	private Long userId;
	public Employee() {
	}

	public Employee(String name, String loginname, String password,
			String phone, String email, Long departmentId, boolean isprimary) {
		super();
		this.name = name;
		this.loginname = loginname;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.departmentId = departmentId;
		this.isprimary = isprimary;
	}

	public static Employee fromJson(final JsonCommand command) {

		final String name = command.stringValueOfParameterNamed("name");
		final String loginname = command
				.stringValueOfParameterNamed("loginname");
		final String password = command.stringValueOfParameterNamed("password");
		final String phone = command.stringValueOfParameterNamed("phone");
		final String email = command.stringValueOfParameterNamed("email");
		final Long departmentId = command
				.longValueOfParameterNamed("departmentId");
		final Boolean isprimary = command
				.booleanObjectValueOfParameterNamed("isprimary");

		return new Employee(name, loginname, password, phone, email,
				departmentId, isprimary);
	}

	public Map<String, Object> update(JsonCommand command) {

		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(
				1);
		final String nameParamName = "name";
		if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
			final String newValue = command
					.stringValueOfParameterNamed(nameParamName);
			actualChanges.put(nameParamName, newValue);
			this.name = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String loginnameParamName = "loginname";
		if (command
				.isChangeInStringParameterNamed(loginnameParamName, this.loginname)) {
			final String newValue = command
					.stringValueOfParameterNamed(loginnameParamName);
			actualChanges.put(loginnameParamName, newValue);
			this.loginname = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String passwordParamName = "password";
		if (command
				.isChangeInStringParameterNamed(passwordParamName, this.password)) {
			final String newValue = command
					.stringValueOfParameterNamed(passwordParamName);
			actualChanges.put(passwordParamName, newValue);
			this.password = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String phoneParamName = "phone";
		if (command
				.isChangeInStringParameterNamed(phoneParamName, this.phone)) {
			final String newValue = command
					.stringValueOfParameterNamed(phoneParamName);
			actualChanges.put(phoneParamName, newValue);
			this.phone = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String emailParamName = "email";
		if (command.isChangeInStringParameterNamed(emailParamName, this.email)) {
			final String newValue = command
					.stringValueOfParameterNamed(emailParamName);
			actualChanges.put(emailParamName, newValue);
			this.email = newValue;
		}
		if (command.isChangeInLongParameterNamed("departmentid",this.departmentId)) {
			final Long newValue = command.longValueOfParameterNamed("departmentid");
			actualChanges.put("departmentid", newValue);
			this.departmentId = newValue;
		}
		if (command.isChangeInBooleanParameterNamed("isprimary", this.isprimary)){
		final boolean newValue =command.booleanPrimitiveValueOfParameterNamed("isprimary");
		actualChanges.put("isprimary", newValue);
		this.isprimary = newValue;
		}
		return actualChanges;

	}

	
	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}
	
	public void delete() {

		if (this.deleted.equalsIgnoreCase("n")) {
			this.deleted = "y";

		}
	}

	public boolean isIsprimary() {
		return isprimary;
	}
}
