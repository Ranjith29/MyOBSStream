package org.obsplatform.portfolio.order.data;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.portfolio.addons.data.AddonsPriceData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;

public class OrderAddonsData {

	private Long id;
	private Long serviceId;
	private Long associateId;

	private String serviceCode;
	private String status;
	private String addOnSerialNo;
	
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate date;
	
	private BigDecimal price;

	private List<AddonsPriceData> addonsPriceDatas;
	private List<SubscriptionData> contractPeriods;
	
	private String planName;
	private Long chargeCodeId;
	private Long planId;
	private String serviceName;
	private Long contractId;
	private Long priceId;
	private Long orderId;

	public OrderAddonsData(final List<AddonsPriceData> addonsPriceDatas,final List<SubscriptionData> contractPeriods) {

		this.addonsPriceDatas = addonsPriceDatas;
		this.contractPeriods = contractPeriods;
	}

	public OrderAddonsData(final Long id, final Long serviceId, final String serviceCode,final LocalDate startDate, final LocalDate endDate,
			final String status,final BigDecimal price,final Long associateId,final String addOnSerialNo) {

		this.id = id;
		this.serviceId = serviceId;
		this.serviceCode = serviceCode;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.price = price;
		this.associateId = associateId;
		this.addOnSerialNo = addOnSerialNo;

	}
	
	public OrderAddonsData(final Long serviceId, final String planName, final Long chargeCodeId, final BigDecimal price) {

		this.serviceId = serviceId;
		this.planName = planName;
		this.chargeCodeId = chargeCodeId; 
		this.price = price;
	}

	public OrderAddonsData(final Long id, final Long planId, final String planName,final Long serviceId, final String serviceName,
			final Long contractId) {

		this.id = id;
		this.planId = planId;
		this.planName = planName;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.contractId = contractId;
	}

	public OrderAddonsData(Long orderPriceId, Long orderId) {
		
		this.priceId = orderPriceId;
		this.orderId = orderId;
		
	}

	public Long getId() {
		return id;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public String getStatus() {
		return status;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public List<AddonsPriceData> getAddonsPriceDatas() {
		return addonsPriceDatas;
	}

	public List<SubscriptionData> getContractPeriods() {
		return contractPeriods;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	public Long getAssociateId() {
		return associateId;
	}

	public String getAddOnSerialNo() {
		return addOnSerialNo;
	}
	
	public String getPlanName() {
		return planName;
	}

	public Long getPriceId() {
		return priceId;
	}

	public Long getOrderId() {
		return orderId;
	}

	public Long getChargeCodeId() {
		return chargeCodeId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public Long getContractId() {
		return contractId;
	}
	
	
	
}
