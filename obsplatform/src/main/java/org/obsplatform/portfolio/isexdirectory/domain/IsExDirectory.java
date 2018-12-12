package org.obsplatform.portfolio.isexdirectory.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * 
 * @author Naresh
 *
 */
@Entity
@Table(name = "b_exdirectory")
public class IsExDirectory extends AbstractAuditableCustom<AppUser, Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "client_id", length = 20, nullable = false)
	private Long clientId;

	@Column(name = "order_id", length = 20, nullable = false)
	private Long orderId;
	
	@Column(name = "plan_id", length = 20)
	private Long planId;

	@Column(name = "service_id", length = 20)
	private Long serviceId;

	@Column(name = "is_ex_directory", length = 1, nullable = false)
	private boolean isExDirectory;

	@Column(name = "is_number_with_held", length = 1, nullable = false)
	private boolean isNumberWithHeld;
	
	@Column(name = "is_umee_app", length = 1, nullable = false)
	private boolean isUmeeApp;

	@Column(name = "is_deleted", length = 1)
	private char isDeleted = 'N';

	public IsExDirectory() {
	}

	public IsExDirectory(final Long clientId, final Long orderId, final Long serviceId, final Long planId, 
			final boolean isExDirectory, final boolean isNumberWithHeld, final boolean isUmeeApp) {
		
		this.clientId = clientId;
		this.orderId = orderId;
		this.serviceId = serviceId;
		this.planId = planId;
		this.isExDirectory = isExDirectory;
		this.isNumberWithHeld = isNumberWithHeld;
		this.isUmeeApp = isUmeeApp;
	}

	public static IsExDirectory fromJson(final JsonCommand command) {

		final Long clientId = command.longValueOfParameterNamed("clientId");
		final Long orderId = command.longValueOfParameterNamed("orderId");
		final Long serviceId = command.longValueOfParameterNamed("serviceId");
		final Long planId = command.longValueOfParameterNamed("planId");
		final boolean isExDirectory = command.booleanObjectValueOfParameterNamed("isExDirectory");
		final boolean isNumberWithHeld = command.booleanObjectValueOfParameterNamed("isNumberWithHeld");
		final boolean isUmeeApp = command.booleanObjectValueOfParameterNamed("isUmeeApp");

		return new IsExDirectory(clientId, orderId, serviceId, planId, isExDirectory, isNumberWithHeld, isUmeeApp);
	}
	
	public Map<String, Object> update(final JsonCommand command) {

		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		final String clientIdParamName = "clientId";
		if (command.isChangeInLongParameterNamed(clientIdParamName, this.clientId)) {
			final Long newValue = command.longValueOfParameterNamed(clientIdParamName);
			actualChanges.put(clientIdParamName, newValue);
			this.clientId = newValue;
		}
		
		final String orderIdParamName = "orderId";
		if (command.isChangeInLongParameterNamed(orderIdParamName, this.orderId)) {
			final Long newValue = command.longValueOfParameterNamed(orderIdParamName);
			actualChanges.put(orderIdParamName, newValue);
			this.orderId = newValue;
		}
		
		final String planIdParamName = "planId";
		if (command.isChangeInLongParameterNamed(planIdParamName, this.planId)) {
			final Long newValue = command.longValueOfParameterNamed(planIdParamName);
			actualChanges.put(planIdParamName, newValue);
			this.planId = newValue;
		}
		
		final String serviceIdParamName = "serviceId";
		if (command.isChangeInLongParameterNamed(serviceIdParamName, this.serviceId)) {
			final Long newValue = command.longValueOfParameterNamed(serviceIdParamName);
			actualChanges.put(serviceIdParamName, newValue);
			this.serviceId = newValue;
		}
		
		final String isExDirectoryParamName = "isExDirectory";
		if (command.isChangeInBooleanParameterNamed(isExDirectoryParamName, this.isExDirectory)) {
			final Boolean newValue = command.booleanObjectValueOfParameterNamed(isExDirectoryParamName);
			actualChanges.put(isExDirectoryParamName, newValue);
			this.isExDirectory = newValue;
		}
		
		final String isNumberWithHeldParamName = "isNumberWithHeld";
		if (command.isChangeInBooleanParameterNamed(isNumberWithHeldParamName, this.isNumberWithHeld)) {
			final Boolean newValue = command.booleanObjectValueOfParameterNamed(isNumberWithHeldParamName);
			actualChanges.put(isNumberWithHeldParamName, newValue);
			this.isNumberWithHeld = newValue;
		}
		
		final String isUmeeAppParamName = "isUmeeApp";
		if (command.isChangeInBooleanParameterNamed(isUmeeAppParamName, this.isUmeeApp)) {
			final Boolean newValue = command.booleanObjectValueOfParameterNamed(isUmeeAppParamName);
			actualChanges.put(isUmeeAppParamName, newValue);
			this.isUmeeApp = newValue;
		}
		
		return actualChanges;

	}
	
	public void delete() {
		if (this.isDeleted == 'N') {
			this.isDeleted = 'Y';
		}
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public Long getPlanId() {
		return planId;
	}

	public void setPlanId(Long planId) {
		this.planId = planId;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public boolean getIsExDirectory() {
		return isExDirectory;
	}

	public void setIsExDirectory(boolean isExDirectory) {
		this.isExDirectory = isExDirectory;
	}

	public boolean getIsNumberWithHeld() {
		return isNumberWithHeld;
	}

	public void setIsNumberWithHeld(boolean isNumberWithHeld) {
		this.isNumberWithHeld = isNumberWithHeld;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}

	public boolean getIsUmeeApp() {
		return isUmeeApp;
	}

	public void setIsUmeeApp(boolean isUmeeApp) {
		this.isUmeeApp = isUmeeApp;
	}
	
}
