package org.obsplatform.organisation.feemaster.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonElement;

@Entity
@Table(name = "b_fee_detail", uniqueConstraints = { @UniqueConstraint(columnNames = { "fee_id", "region_id","plan_id","contract_period"},
   name = "feeid_with_region_uniquekey") })
public class FeeDetail extends AbstractPersistable<Long>{

	private static final long serialVersionUID = 1L;

	@Column(name = "region_id")
	private String regionId;

	@Column(name = "amount")
	private BigDecimal amount;
	
	@Column(name = "plan_id")
	private Long planId;
	
	@Column(name = "contract_period")
	private String contractPeriod;
	
	@Column(name = "category_id")
	private Long categoryId;
	
	@ManyToOne
    @JoinColumn(name="fee_id")
	private FeeMaster feeMaster;
	
	@Column(name = "is_deleted", nullable = false)
	private char isDeleted='N';
	
	public FeeDetail(){
		
	}

	public FeeDetail(final String regionId, final BigDecimal amount, final Long planId, final String contractPeriod,
			final Long categoryId) {
		
		this.regionId = regionId;
		this.amount = amount;
		this.planId = planId;
		this.contractPeriod = contractPeriod;
		this.categoryId = categoryId;
	}

	public void update(FeeMaster feeMaster) {
		
		this.feeMaster = feeMaster;
	}
	
	public Map<String, Object> update(final JsonElement element,final FromJsonHelper fromApiJsonHelper) {
		
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		
		final String regionIdParamName = "regionId";
		final String regionId = fromApiJsonHelper.extractStringNamed("regionId", element);
		if (this.differenceExists(this.regionId,regionId)) {
			final String newValue = regionId;
			actualChanges.put(regionIdParamName, newValue);
			this.regionId = StringUtils.defaultIfEmpty(newValue, null);
		}
		
		final String amountParamName = "amount";
		final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
		if (this.amount != null && this.amount.compareTo(amount) !=0) {
			actualChanges.put(amountParamName, amount);
			this.amount = amount;
		}
		
		final String planIdParamName = "planId";
		final Long planId = fromApiJsonHelper.extractLongNamed("planId", element);
		if(this.differenceExistsOnNumbers(this.planId, planId) ){
			if (planId != null) {
				actualChanges.put(planIdParamName, planId);
			}
			this.planId = planId;
		}
		
		final String contractPeriodParamName = "contractPeriod";
		final String contractPeriod = fromApiJsonHelper.extractStringNamed("contractPeriod", element);
		if (this.differenceExists(this.contractPeriod, contractPeriod)) {
			if(contractPeriod != null){
			  actualChanges.put(contractPeriodParamName, contractPeriod);
			}
			if(this.planId !=null && this.planId > 0)
			 this.contractPeriod = StringUtils.defaultIfEmpty(contractPeriod, null);
		}
		
		final String categoryIdParamName = "categoryId";
		final Long categoryId = fromApiJsonHelper.extractLongNamed("categoryId", element);
		if(this.differenceExistsOnNumbers(this.categoryId, categoryId) ){
			if (categoryId != null) {
				actualChanges.put(categoryIdParamName, categoryId);
			}
			this.categoryId = categoryId;
		}
		
		return actualChanges;
	}
	
	private boolean differenceExists(final String baseValue, final String newCopyValue) {
        boolean differenceExists = false;

        if (StringUtils.isNotBlank(baseValue)) {
            differenceExists = !baseValue.equals(newCopyValue);
        } else {
            differenceExists = StringUtils.isNotBlank(newCopyValue);
        }

        return differenceExists;
    }
	
	private boolean differenceExistsOnNumbers(final Number baseValue, final Number newCopyValue) {
        boolean differenceExists = false;

        if (baseValue != null) {
            differenceExists = !baseValue.equals(newCopyValue);
        } else {
            differenceExists = (newCopyValue != null && !newCopyValue.equals(0));
        }

        return differenceExists;
    }
	

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public Long getPlanId() {
		return planId;
	}

	public void setPlanId(Long planId) {
		this.planId = planId;
	}

	public String getContractPeriod() {
		return contractPeriod;
	}

	public void setContractPeriod(String contractPeriod) {
		this.contractPeriod = contractPeriod;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public void deleted() {
		
	this.isDeleted = 'Y';
	this.regionId = this.regionId+"_"+this.getId()+"_Y";
		
	}

	

}
