package org.obsplatform.logistics.onetimesale.data;

import org.joda.time.LocalDate;

public class AllocationDetailsData {

	private Long id;
	private String itemDescription;
	private String serialNo;
	private LocalDate allocationDate;
	private Long itemDetailId;
	private String allocationType;
	private String quality;
	private String hardwareStatus;
	private Long orderId;
	private Long serviceId;
	private Long planId;
	private Long clientId;
	private String serialno;
	private LocalDate warrantyDate;
	

	public AllocationDetailsData(final Long id, final String itemDescription,
			final String serialNo, final LocalDate allocationDate, final Long itemDetailId, String allocationType,
			String quality, String hardwareStatus, LocalDate warrantyDate) {
		this.id = id;
		this.itemDescription = itemDescription;
		this.serialNo = serialNo;
		this.allocationDate = allocationDate;
		this.itemDetailId = itemDetailId;
		this.allocationType=allocationType;
		this.quality = quality;
		this.hardwareStatus = hardwareStatus;
		this.warrantyDate = warrantyDate;
	}

	public AllocationDetailsData(final Long id, final Long orderId, final String serialNum,
			final Long clientId) {

		this.id = id;
		this.serialNo = serialNum;

	}

	/**
	 * @param serviceId
	 * @param planId
	 * @param allocationType
	 * @param serialNum
	 * @param itemDescription
	 */
	public AllocationDetailsData(final Long serviceId, final Long planId,final Long orderId,
			final String allocationType, final String serialNum, final String itemDescription) {
		
		this.serviceId = serviceId;
		this.planId = planId;
		this.orderId = orderId;
		this.allocationType = allocationType;
		this.serialNo = serialNum;
		this.itemDescription = itemDescription;
	}

	/**
	 * @param id
	 * @param serviceId
	 * @param planId
	 * @param orderId
	 * @param serialNum
	 * @param clientId
	 */
	public AllocationDetailsData(final Long id, final Long serviceId, final Long planId,
			                   final Long orderId, final String serialNum, final Long clientId,final String serialno) {
		
		this.id = id;
		this.serviceId = Long.valueOf(0).equals(serviceId) ? null: serviceId;
		this.planId = planId;
		this.orderId = orderId;
		this.serialNo = serialNum;
		this.clientId = clientId;
		this.serialno = serialno;
	}

	public Long getId() {
		return id;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getSerialNo() {
		return serialNo;
	}
	
	

	public String getAllocationType() {
		return allocationType;
	}

	public LocalDate getAllocationDate() {
		return allocationDate;
	}

	public Long getItemDetailId() {
		return itemDetailId;
	}

	public String getQuality() {
		return quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	public String getHardwareStatus() {
		return hardwareStatus;
	}

	public void setHardwareStatus(String hardwareStatus) {
		this.hardwareStatus = hardwareStatus;
	}

	/**
	 * @return the orderId
	 */
	public Long getOrderId() {
		return orderId;
	}

	/**
	 * @return the serviceId
	 */
	public Long getServiceId() {
		return serviceId;
	}

	/**
	 * @return the planId
	 */
	public Long getPlanId() {
		return planId;
	}

	/**
	 * @return the clientId
	 */
	public Long getClientId() {
		return clientId;
	}

	public String getSerialno() {
		return serialno;
	}

	public LocalDate getWarrantyDate() {
		return warrantyDate;
	}

	public void setWarrantyDate(LocalDate warrantyDate) {
		this.warrantyDate = warrantyDate;
	}
	
	
}
