package org.obsplatform.organisation.employee.data;

import java.util.Collection;

import org.obsplatform.organisation.department.data.DepartmentData;

public class EmployeeData {
	private Long id;
	private String name;
	private String loginname;
	private String password;
	private Long phone;
	private String email;
	private boolean isprimary;
	private Collection<DepartmentData> departmentdata;
	private Long departmentId;
	private String departmentName;
	private String allocated;
	private Long userId;
	//EmployeeData 0-param constructor
	public EmployeeData() {
	}

	//it is for template purpose
	public EmployeeData(Collection<DepartmentData> departmentdata) {
		this.departmentdata = departmentdata;
	}

	//it is for without template purpose
	public EmployeeData(Long id, String name, String loginname,
			String password, Long phone, String email,
			boolean isprimary, Long departmentId, String departmentName, String allocated, Long userId) {
		this.id = id;
		this.name = name;
		this.loginname = loginname;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.isprimary = isprimary;
		this.departmentId = departmentId;
		this.departmentName = departmentName;
		this.allocated = allocated;
		this.userId = userId;
	}

	public EmployeeData(String name, String loginname, String password,Long phone, String email, boolean isprimary) {
		this.name = name;
		this.loginname = loginname;
		this.password = password;
		this.phone = phone;
		this.email = email;
		this.isprimary = isprimary;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginname() {
		return loginname;
	}

	public void setLoginname(String loginname) {
		this.loginname = loginname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public Long getPhone() {
		return phone;
	}

	public void setPhone(Long phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isIsprimary() {
		return isprimary;
	}

	public void setIsprimary(boolean isprimary) {
		this.isprimary = isprimary;
	}

	public Collection<DepartmentData> getDepartmentdata() {
		return departmentdata;
	}

	public void setDepartmentdata(Collection<DepartmentData> departmentdata) {
		this.departmentdata = departmentdata;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	
	
}
