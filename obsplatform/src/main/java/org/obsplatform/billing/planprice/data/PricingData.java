package org.obsplatform.billing.planprice.data;

import java.math.BigDecimal;
import java.util.List;

import org.obsplatform.billing.chargecode.data.ChargeCodeData;
import org.obsplatform.billing.chargevariant.data.ChargeVariantData;
import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.organisation.priceregion.data.PriceRegionData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.plan.data.ServiceData;


public class PricingData {

	private List<ServiceData> serviceData;
	private List<ChargeCodeData> chargeData;
	private List<ChargeVariantData> chargevariant;
	private List<DiscountMasterData> discountdata;
	private String planCode;
	private Long planId;
	private Long serviceId;
	private Long chargeId;
	private BigDecimal price;
	private Long discountId;
	private Long chargeVariantId;
	private Long id;
	private List<PriceRegionData> priceRegionData;
	private Long priceregion;
	private Long priceId;
	private String serviceCode;
	private String chargeCode;
	private Long contractId;
	private String contractPeriod;
	private List<SubscriptionData> contractPeriods;
	private String isPrepaid;
	private List<ServiceData> pricingData;
	private String variantCode;
	private Long amount;

	public PricingData(final List<ServiceData> serviceData,	final List<ChargeCodeData> chargeData,
	final List<ChargeVariantData> chargevariant,final List<DiscountMasterData> data,final String planCode,final Long planId,final PricingData pricingData, 
	final List<PriceRegionData> priceRegionData,final List<SubscriptionData> contractPeriods,final String isPrepaid, final Long price)
	{

		if(pricingData!= null)
		{
		this.chargeId=pricingData.getChargeId();
		this.serviceId=pricingData.getServiceId();
		this.price=pricingData.getPrice();
		this.discountId=pricingData.getDiscountId();
		this.chargeVariantId=pricingData.getChargeVariantId();
		this.priceregion=pricingData.getPriceregion();
		this.planCode=pricingData.getPlanCode();
		this.priceId=pricingData.getPriceId();
		this.serviceCode=pricingData.getServiceCode();
		this.chargeCode=pricingData.getChargeCode();
		this.contractPeriod=pricingData.getContractPeriod();
		this.variantCode=pricingData.getVariantCode();
		
		}
		this.chargeData=chargeData;
		this.serviceData=serviceData;
		this.chargevariant=chargevariant;
		this.discountdata=data;
		if (planCode != null) {
			this.planCode = planCode;
		}
		this.planId=planId;
		this.isPrepaid=isPrepaid;
		this.priceRegionData=priceRegionData;
		this.contractPeriods=contractPeriods;
		this.amount = price;
	}

	public PricingData(final List<ServiceData> serviceData) {
		this.pricingData=serviceData;
	}

	public PricingData(final Long id,final String serviceCode,final String chargeCode,final BigDecimal price,final Long discountId,final Long chargeVariantId, 
			final Long priceregion,final String planCode,final Long priceId,final String contractperiod,final String variantCode) {
	
		this.planId=id;
		this.serviceCode=serviceCode;
		this.chargeCode=chargeCode;
		this.price=price;
		this.contractPeriod=contractperiod;
		this.chargeVariantId=chargeVariantId;
		this.discountId=discountId;
		this.priceregion=priceregion;
		this.planCode=planCode;
		this.priceId=priceId;
		this.variantCode=variantCode;

	}


	public PricingData(Long planId, String planCode, String isPrepaid,List<ServiceData> pricingData) {
		
		this.planId=planId;
		this.planCode=planCode;
		this.isPrepaid=isPrepaid;
		this.pricingData=pricingData;
	}

	public List<ServiceData> getServiceData() {
		return serviceData;
	}
	public Long getContractId() {
		return contractId;
	}

	public String getContractPeriod() {
		return contractPeriod;
	}

	public List<SubscriptionData> getContractPeriods() {
		return contractPeriods;
	}

	public String getIsPrepaid() {
		return isPrepaid;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public Long getPriceId() {
		return priceId;
	}

	public List<ChargeCodeData> getChargeData() {
		return chargeData;
	}

	public List<ChargeVariantData> getChargevariant() {
		return chargevariant;
	}

	public List<DiscountMasterData> getDiscountdata() {
		return discountdata;
	}

	public Long getPlanId() {
		return planId;
	}

	public String getPlanCode() {
		return planCode;
	}

	public Long getServiceId() {
		return serviceId;
	}

	public Long getChargeId() {
		return chargeId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public Long getDiscountId() {
		return discountId;
	}

	public Long getChargeVariantId() {
		return chargeVariantId;
	}

	public Long getId() {
		return id;
	}

	public List<PriceRegionData> getPriceRegionData() {
		return priceRegionData;
	}

	public Long getPriceregion() {
		return priceregion;
	}

	public List<ServiceData> getPricingData() {
		return pricingData;
	}
	
	public String getVariantCode() {
		return variantCode;
	}

	
}
