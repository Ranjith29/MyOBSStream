package org.obsplatform.portfolio.isexdirectory.data;

/**
 * 
 * @author Naresh
 *
 */

public class IsExDirectoryData {
	
	private final Long id;
	private final Long clientId;
	private final Long orderId;
	private final Long planId;
	private final Long serviceId;
	private final boolean isExDirectory;
	private final boolean isNumberWithHeld;
	private final boolean isUmeeApp;

	public IsExDirectoryData(final Long id, final Long clientId, final Long orderId, final Long planId, final Long serviceId, 
			final boolean isExDirectory, final boolean isNumberWithHeld, final boolean isUmeeApp) {
		
		this.id = id;
		this.clientId = clientId;
		this.orderId = orderId;
		this.planId = planId;
		this.serviceId = serviceId;
		this.isExDirectory = isExDirectory;
		this.isNumberWithHeld = isNumberWithHeld;
		this.isUmeeApp = isUmeeApp;
	}

	public Long getId() {
		return id;
	}

	public Long getClientId() {
		return clientId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Long getPlanId() {
		return planId;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public boolean isExDirectory() {
		return isExDirectory;
	}

	public boolean isNumberWithHeld() {
		return isNumberWithHeld;
	}

	public boolean isUmeeApp() {
		return isUmeeApp;
	}
	
}
