package org.obsplatform.provisioning.wifimaster.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * @author anil
 *
 */

@Entity
@Table(name = "b_wifi_details", uniqueConstraints = { @UniqueConstraint(columnNames = { "order_id" }, name = "uq_wd_orderId")} )
public class WifiMaster extends AbstractPersistable<Long> {
	

	private static final long serialVersionUID = 1L;
	
	@Column(name = "client_id", length = 20, nullable = false)
	private Long clientId;

	@Column(name = "order_id")
	private Long orderId;
	
	@Column(name = "service_id")
	private Long serviceId;
	
	
	@Column(name = "ssid", length = 20, nullable = false)
	private String ssid;

	@Column(name = "wifi_password", nullable = false)
	private String wifiPassword;

	@Column(name = "service_type", length = 15, nullable = false)
	private String serviceType;
	
	@Column(name = "is_deleted")
	char is_deleted = 'N';
	

	public WifiMaster() {
	}
	
	
	public WifiMaster(Long clientId, String ssid, String wifiPassword,
			String serviceType,Long orderId,Long serviceId) {
		this.clientId = clientId;
		this.ssid = ssid;
		this.wifiPassword = wifiPassword;
		this.serviceType = serviceType;
		this.orderId=orderId;
		this.serviceId=serviceId;
	}


	public static WifiMaster fromJson(final JsonCommand command) {

		final Long clientId = command.longValueOfParameterNamed("clientId");
		final String ssid = command.stringValueOfParameterNamed("ssid");
		final String wifiPassword = command.stringValueOfParameterNamed("wifiPassword");
		final String serviceType = command.stringValueOfParameterNamed("serviceType");
		final Long orderId = command.longValueOfParameterNamed("orderId");
		final Long serviceId = command.longValueOfParameterNamed("serviceId");

		return new WifiMaster(clientId, ssid, wifiPassword, serviceType,orderId,serviceId);
	}


	public Map<String, Object> update(JsonCommand command) {
		
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		
		if (command.isChangeInStringParameterNamed("ssid", this.ssid)) {
			final String newValue = command.stringValueOfParameterNamed("ssid");
			actualChanges.put("ssid", newValue);
			this.ssid = newValue;
		}
		if (command.isChangeInStringParameterNamed("wifiPassword", this.wifiPassword)) {
			final String newValue = command.stringValueOfParameterNamed("wifiPassword");
			actualChanges.put("wifiPassword", newValue);
			this.wifiPassword = newValue;
		}
		if (command.isChangeInStringParameterNamed("serviceType", this.serviceType)) {
			final String newValue = command.stringValueOfParameterNamed("serviceType");
			actualChanges.put("serviceType", newValue);
			this.serviceType = newValue;
		}
		
		return actualChanges;
	}


	public String getSsid() {
		return ssid;
	}

	public void setSsid(String ssid) {
		this.ssid = ssid;
	}

	public String getWifiPassword() {
		return wifiPassword;
	}

	public void setWifiPassword(String wifiPassword) {
		this.wifiPassword = wifiPassword;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	

	public Long getOrderId() {
		return orderId;
	}


	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}


	public Long getServiceId() {
		return serviceId;
	}


	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}


	public char getIs_deleted() {
		return is_deleted;
	}


	public void setIs_deleted(char is_deleted) {
		this.is_deleted = is_deleted;
	}

	public void delete(){
		if(this.is_deleted == 'N'){
			this.is_deleted = 'Y';
		}
	}

	

}
