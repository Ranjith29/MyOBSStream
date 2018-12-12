package org.obsplatform.provisioning.wifimaster.data;

import org.obsplatform.infrastructure.core.api.JsonCommand;

public class WifiData {

	private Long id;
	private Long clientId;
	private String ssid;
	private String wifiPassword;
	private String serviceType;
	private Long orderId;
	private Long serviceId;

	public WifiData(Long id,  Long clientId, String ssid,
			String wifiPassword, String serviceType,Long orderId,Long serviceId) {

		this.id = id;
		this.clientId=clientId;
		this.ssid = ssid;
		this.wifiPassword = wifiPassword;
		this.serviceType = serviceType;
		this.orderId=orderId;
		this.serviceId=serviceId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	

	public static WifiData fromJson(JsonCommand command) {

		final Long id = command.longValueOfParameterNamed("id");
		final Long clientId = command.longValueOfParameterNamed("clientId");
		final String ssid = command.stringValueOfParameterNamed("ssid");
		final String wifiPassword = command.stringValueOfParameterNamed("wifiPassword");
		final String serviceType = command.stringValueOfParameterNamed("serviceType");
		final Long orderId = command.longValueOfParameterNamed("orderId");
		final Long serviceId = command.longValueOfParameterNamed("serviceId");

		return new WifiData(id, clientId, ssid, wifiPassword, serviceType,orderId,serviceId);
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
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
	

}
