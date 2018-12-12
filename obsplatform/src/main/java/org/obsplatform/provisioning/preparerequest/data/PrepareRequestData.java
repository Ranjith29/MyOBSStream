package org.obsplatform.provisioning.preparerequest.data;

public class PrepareRequestData {
	
	private final Long requestId;
	private final Long clientId;
	private final Long orderId;
	private final Long addonId;
	private final String requestType;
	private final String hardwareId;
	private final String userName;
	private final String provisioningSystem;
	private final String planName;
	private final String ishardwareReq;
	private final String phoneNumber;

	public PrepareRequestData(final Long requestId, final Long clientId, final Long orderId,final String requestType, final String hardWareId,
			final String userName,final String provisioningSys, final String planName, final String ishwReq,final Long addonId, final String phoneNumber) {
		
		this.requestId = requestId;
		this.clientId = clientId;
		this.orderId = orderId;
		this.requestType = requestType;
		this.hardwareId = hardWareId;
		this.userName = userName;
		this.provisioningSystem = provisioningSys;
		this.planName = planName;
		this.ishardwareReq = ishwReq;
		this.addonId = addonId;
		this.phoneNumber = phoneNumber;

	}

	public Long getRequestId() {
		return requestId;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getRequestType() {
		return requestType;
	}

	public Long getAddonId() {
		return addonId;
	}

	public String getHardwareId() {
		return hardwareId;
	}

	public String getUserName() {
		return userName;
	}
	
	public String getProvisioningSystem() {
		return provisioningSystem;
	}

	public String getPlanName() {
		return planName;
	}
	
	public String getIshardwareReq() {
		return ishardwareReq;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

}
