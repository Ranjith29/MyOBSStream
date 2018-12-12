package org.obsplatform.portfolio.order.data;

public class OrderLineData {

	private final Long id;
	private final Long orderId;
	private final String servicecode;
	private final String serviceDescription;
	private final String serviceType;
	private final Long serviceId;
	private String isAutoProvision;
	private String image;
	private String serialNo;
	private Long associateId;
	private String allocationType;
	private Long itemId;

	public OrderLineData(Long id, Long orderId, String serviceCode,String serviceDescription, String serviceType, Long serviceId,
			String isAutoProvision, String image,Long associateId, String serialNo,String allocationType, Long itemId) {

		this.id = id;
		this.orderId = orderId;
		this.servicecode = serviceCode;
		this.serviceDescription = serviceDescription;
		this.serviceType = serviceType;
		this.serviceId = serviceId;
		this.isAutoProvision = isAutoProvision;
		this.image = image;
		this.associateId = associateId.equals(Long.valueOf(0)) ? null:associateId;
		this.serialNo = serialNo;
		this.allocationType = allocationType;
		this.itemId = itemId.equals(Long.valueOf(0)) ? null : itemId;
	}

	public Long getId() {
		return id;
	}

	public Long getOrderId() {
		return orderId;
	}

	public String getServicecode() {
		return servicecode;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public String getServiceType() {
		return serviceType;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public String getIsAutoProvision() {
		return isAutoProvision;
	}

	public String getImage() {
		return image;
	}

	public Long getAssociateId() {
		return associateId;
	}

	public String getSerialNo() {
		return serialNo;
	}

	public String getAllocationType() {
		return allocationType;
	}

	public Long getItemId() {
		return itemId;
	}
	
}
