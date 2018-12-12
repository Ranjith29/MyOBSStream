package org.obsplatform.organisation.feemaster.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.obsplatform.billing.chargecode.data.ChargesData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.region.data.RegionData;
import org.obsplatform.portfolio.client.service.ClientCategoryData;
import org.obsplatform.portfolio.contract.data.SubscriptionData;
import org.obsplatform.portfolio.plan.data.PlanData;

public class FeeMasterData {

	private Long id;
	private String feeCode;
	private String feeDescription;
	private String transactionType;
	private String chargeCode;
	private BigDecimal defaultFeeAmount;
	private Long feeId;
	private Long regionId;
	private String regionName;
	private BigDecimal amount;
	private FeeMasterData feeMasterData;
	private String isRefundable;
	private Long planId;
	private String planName;
	private String contractPeriod;
	private List<ChargesData> chargeDatas;
	private List<RegionData> regionDatas;
	private Collection<MCodeData> transactionTypeDatas;
	private List<FeeMasterData> feeMasterRegionPricesDatas;
	private List<PlanData> planDatas;
	private List<SubscriptionData> subscriptionDatas;
	private Boolean enabled;
	private Collection<ClientCategoryData> categoryDatas;
	private Long categoryId;
	private String categoryType;

	public FeeMasterData(final Long id, final String feeCode, final String feeDescription,final String transactionType, 
			final String chargeCode,final BigDecimal defaultFeeAmount, final String isRefundable, final Boolean enabled) {

		this.id = id;
		this.feeCode = feeCode;
		this.feeDescription = feeDescription;
		this.transactionType = transactionType;
		this.chargeCode = chargeCode;
		this.defaultFeeAmount = defaultFeeAmount;
		this.isRefundable = isRefundable;
		this.enabled = enabled;
	}

	public FeeMasterData(Collection<MCodeData> transactionTypeDatas,List<ChargesData> chargeDatas, List<RegionData> regionDatas, 
			List<PlanData> planDatas, List<SubscriptionData> subscriptionDatas, Collection<ClientCategoryData> categoryDatas) {

		this.transactionTypeDatas = transactionTypeDatas;
		this.chargeDatas = chargeDatas;
		this.regionDatas = regionDatas;
		this.planDatas = planDatas;
		this.subscriptionDatas = subscriptionDatas;
		this.categoryDatas = categoryDatas;
	}

	public FeeMasterData(final Long id, final Long feeId, final Long regionId, final String regionName, final BigDecimal amount, 
			final Long planId, final String planName, final String contractPeriod, final Long categoryId, final String categoryType) {

		this.id = id;
		this.feeId = feeId;
		this.regionId = regionId;
		this.regionName = regionName;
		this.amount = amount;
		this.planId = planId;
		this.planName = planName;
		this.contractPeriod = contractPeriod;
		this.categoryId = categoryId;
		this.categoryType = categoryType;
	}

	public FeeMasterData(FeeMasterData feeMasterData,Collection<MCodeData> transactionTypeDatas,
			List<ChargesData> chargeDatas, List<RegionData> regionDatas,List<FeeMasterData> feeMasterRecategoryIdgionPricesDatas) {

		this.feeMasterData = feeMasterData;
		this.transactionTypeDatas = transactionTypeDatas;
		this.chargeDatas = chargeDatas;
		this.regionDatas = regionDatas;
		this.feeMasterRegionPricesDatas = feeMasterRegionPricesDatas;
	}

	public FeeMasterData(final Long id, final String feeCode, final String feeDescription, final String transactionType, 
			final String chargeCode, final BigDecimal defaultFeeAmount, final Long categoryId) {
		
		this.id = id;
		this.feeCode = feeCode;
		this.feeDescription = feeDescription;
		this.transactionType = transactionType;
		this.chargeCode = chargeCode;
		this.defaultFeeAmount = defaultFeeAmount;
		this.categoryId = categoryId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFeeCode() {
		return feeCode;
	}

	public void setFeeCode(String feeCode) {
		this.feeCode = feeCode;
	}

	public String getFeeDescription() {
		return feeDescription;
	}

	public void setFeeDescription(String feeDescription) {
		this.feeDescription = feeDescription;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	public BigDecimal getDefaultFeeAmount() {
		return defaultFeeAmount;
	}

	public void setDefaultFeeAmount(BigDecimal defaultFeeAmount) {
		this.defaultFeeAmount = defaultFeeAmount;
	}

	public List<ChargesData> getChargeDatas() {
		return chargeDatas;
	}

	public void setChargeDatas(List<ChargesData> chargeDatas) {
		this.chargeDatas = chargeDatas;
	}

	public List<RegionData> getRegionDatas() {
		return regionDatas;
	}

	public void setRegionDatas(List<RegionData> regionDatas) {
		this.regionDatas = regionDatas;
	}

	public Long getFeeId() {
		return feeId;
	}

	public void setFeeId(Long feeId) {
		this.feeId = feeId;
	}

	public Long getRegionId() {
		return regionId;
	}

	public void setRegionId(Long regionId) {
		this.regionId = regionId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public FeeMasterData getFeeMasterData() {
		return feeMasterData;
	}

	public void setFeeMasterData(FeeMasterData feeMasterData) {
		this.feeMasterData = feeMasterData;
	}

	public Collection<MCodeData> getTransactionTypeDatas() {
		return transactionTypeDatas;
	}

	public void setTransactionTypeDatas(Collection<MCodeData> transactionTypeDatas) {
		this.transactionTypeDatas = transactionTypeDatas;
	}

	public List<FeeMasterData> getFeeMasterRegionPricesDatas() {
		return feeMasterRegionPricesDatas;
	}

	public void setFeeMasterRegionPricesDatas(List<FeeMasterData> feeMasterRegionPricesDatas) {
		this.feeMasterRegionPricesDatas = feeMasterRegionPricesDatas;
	}

	public String getIsRefundable() {
		return isRefundable;
	}

	public String getRegionName() {
		return regionName;
	}

	public Long getPlanId() {
		return planId;
	}

	public String getPlanName() {
		return planName;
	}

	public String getContractPeriod() {
		return contractPeriod;
	}

	public List<PlanData> getPlanDatas() {
		return planDatas;
	}

	public void setPlanDatas(List<PlanData> planDatas) {
		this.planDatas = planDatas;
	}

	public List<SubscriptionData> getSubscriptionDatas() {
		return subscriptionDatas;
	}

	public void setSubscriptionDatas(List<SubscriptionData> subscriptionDatas) {
		this.subscriptionDatas = subscriptionDatas;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Collection<ClientCategoryData> getCategoryDatas() {
		return categoryDatas;
	}

	public void setCategoryDatas(Collection<ClientCategoryData> categoryDatas) {
		this.categoryDatas = categoryDatas;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public String getCategoryType() {
		return categoryType;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}
	

}
