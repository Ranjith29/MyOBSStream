package org.obsplatform.portfolio.servicepartnermapping.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * 
 * @author Naresh
 * 
 */
@Entity
@Table(name = "b_service_partner_mapping", uniqueConstraints = { @UniqueConstraint(name = "service_code_uq", columnNames = { "service_id" }) })
public class ServicePartnerMapping extends AbstractAuditableCustom<AppUser, Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "partner_name", nullable = false, length = 100)
	private String partnerName;
	
	@Column(name = "is_deleted", length = 2)
	private char isDeleted = 'N';

	public ServicePartnerMapping() {

	}

	public ServicePartnerMapping(final Long serviceId, final String partnerName) {

		this.serviceId = serviceId;
		this.partnerName = partnerName;
	}

	public static ServicePartnerMapping fromJson(JsonCommand command) {

		final Long serviceId = command.longValueOfParameterNamed("serviceId");
		final String partnerName = command.stringValueOfParameterNamed("partnerName");

		return new ServicePartnerMapping(serviceId, partnerName);
	}

	public Map<String, Object> update(JsonCommand command) {

		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);

		final String serviceParamName = "serviceId";
		if (command.isChangeInLongParameterNamed(serviceParamName, this.serviceId)) {
			final Long newValue = command.longValueOfParameterNamed(serviceParamName);
			actualChanges.put(serviceParamName, newValue);
			this.serviceId = newValue;
		}

		final String partnerNameParamName = "partnerName";
		if (command.isChangeInStringParameterNamed(partnerNameParamName, this.partnerName)) {
			final String newValue = command.stringValueOfParameterNamed(partnerNameParamName);
			actualChanges.put(partnerNameParamName, newValue);
			this.partnerName = StringUtils.defaultIfEmpty(newValue, null);
		}

		return actualChanges;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public void delete() {
		if (this.isDeleted == 'N') {
			this.isDeleted = 'Y';
		}
	}


}
