package org.obsplatform.organisation.ticketassignrule.data;

import java.util.Collection;

import org.obsplatform.organisation.department.data.DepartmentData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;

public class TicketAssignRuleData {

	private Long id;
	private Long clientCategoryId;
	private Long departmentId;
	private Long businessProcessId;
	private String businessprocessName;
	private String clietcategoryType;
	private String departmentName;
	private Collection<ClientCategoryData> clientCategoryData;
	private Collection<MCodeData> businessprocessCodes;
	private Collection<DepartmentData> departmentdatas;
	
	public TicketAssignRuleData(final Long id,final Long clientCategoryId,final Long departmentId,final Long businessProcessId,
			final String businessprocessName,final String clietcategoryType,final String departmentName){
		this.id = id;
		this.clientCategoryId = clientCategoryId;
		this.departmentId = departmentId;
		this.businessProcessId = businessProcessId;
		this.businessprocessName = businessprocessName;
		this.clietcategoryType = clietcategoryType;
		this.departmentName = departmentName;
	}
	
	public TicketAssignRuleData(Collection<ClientCategoryData> clientCategoryData,Collection<MCodeData> businessprocessCodes,
			Collection<DepartmentData> departmentdatas){
		this.clientCategoryData = clientCategoryData;
		this.businessprocessCodes = businessprocessCodes;
		this.departmentdatas = departmentdatas;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getClientCategoryId() {
		return clientCategoryId;
	}

	public void setClientCategoryId(Long clientCategoryId) {
		this.clientCategoryId = clientCategoryId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}

	public Long getBusinessProcessId() {
		return businessProcessId;
	}

	public void setBusinessProcessId(Long businessProcessId) {
		this.businessProcessId = businessProcessId;
	}

	public Collection<ClientCategoryData> getClientCategoryData() {
		return clientCategoryData;
	}

	public void setClientCategoryData(Collection<ClientCategoryData> clientCategoryData) {
		this.clientCategoryData = clientCategoryData;
	}

	public Collection<MCodeData> getBusinessprocessCodes() {
		return businessprocessCodes;
	}

	public void setBusinessprocessCodes(Collection<MCodeData> businessprocessCodes) {
		this.businessprocessCodes = businessprocessCodes;
	}

	public Collection<DepartmentData> getDepartmentdatas() {
		return departmentdatas;
	}

	public void setDepartmentdatas(Collection<DepartmentData> departmentdatas) {
		this.departmentdatas = departmentdatas;
	}

	public String getBusinessprocessName() {
		return businessprocessName;
	}

	public void setBusinessprocessName(String businessprocessName) {
		this.businessprocessName = businessprocessName;
	}

	public String getClietcategoryType() {
		return clietcategoryType;
	}

	public void setClietcategoryType(String clietcategoryType) {
		this.clietcategoryType = clietcategoryType;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
}
