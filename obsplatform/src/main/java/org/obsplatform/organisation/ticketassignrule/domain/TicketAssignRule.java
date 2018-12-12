package org.obsplatform.organisation.ticketassignrule.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name="b_ticketassign_rule", uniqueConstraints = { @UniqueConstraint(columnNames = { "businessprocess_id", "clientcategory_id"},
name = "businessprocessid_with_categorytype_uniquekey") })
public class TicketAssignRule extends AbstractAuditableCustom<AppUser, Long>{

	private static final long serialVersionUID = 1L;

	@Column(name="clientcategory_id")
	private Long clientCategoryId;
	
	@Column(name="department_id")
	private Long departmentId;
	
	@Column(name="businessprocess_id")
	private Long businessprocessId;
	
	@Column(name = "is_deleted")
	private char isDeleted='N';
	
	public TicketAssignRule(){
		
	}
	
	public TicketAssignRule(Long clientCategoryId,Long departmentId,Long businessProcessId){
		this.clientCategoryId = clientCategoryId;
		this.departmentId = departmentId;
		this.businessprocessId = businessProcessId;
	}
	
	public static TicketAssignRule fromJson(final JsonCommand command){
		final Long clientcategoryid = command.longValueOfParameterNamed("clientcategoryId");
		final Long departmentid = command.longValueOfParameterNamed("departmentId");
		final Long businessprocessid = command.longValueOfParameterNamed("businessprocessId");
		return new TicketAssignRule(clientcategoryid, departmentid, businessprocessid);
	}
	
	 public Map<String, Object> update(final JsonCommand command) {

		  final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		  
			if (command.isChangeInLongParameterNamed("clientcategoryId",this.clientCategoryId)) {
				final Long newValue = command.longValueOfParameterNamed("clientcategoryId");
				actualChanges.put("clientcategoryId", newValue);
				this.clientCategoryId = newValue;
			}
			if (command.isChangeInLongParameterNamed("departmentId",this.departmentId)) {
				final Long newValue = command.longValueOfParameterNamed("departmentId");
				actualChanges.put("departmentId", newValue);
				this.departmentId = newValue;
			}
			if (command.isChangeInLongParameterNamed("businessprocessId",this.businessprocessId)) {
				final Long newValue = command.longValueOfParameterNamed("businessprocessId");
				actualChanges.put("businessprocessId", newValue);
				this.businessprocessId = newValue;
			}

     return actualChanges;

	}
	
	public void delete() {
		this.isDeleted = 'Y';
		this.businessprocessId = Long.valueOf(this.businessprocessId+""+this.getId());
	}

	public Long getClientCategoryId() {
		return clientCategoryId;
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public Long getBusinessprocessId() {
		return businessprocessId;
	}
}
