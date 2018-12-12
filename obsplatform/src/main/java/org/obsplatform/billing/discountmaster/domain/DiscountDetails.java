package org.obsplatform.billing.discountmaster.domain;


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
@Table(name = "b_discount_details", uniqueConstraints = { @UniqueConstraint(columnNames = {"discount_id", "category_type" }, name = "discountid_with_category_uniquekey") })
public class DiscountDetails extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "category_type")
	private String categoryType;

	@Column(name = "discount_rate")
	private BigDecimal discountRate;

	@Column(name = "is_deleted")
	private String isDeleted = "N";

	@ManyToOne
	@JoinColumn(name = "discount_id")
	private DiscountMaster discountMaster;

	public DiscountDetails() {
	}

	public DiscountDetails(final String categoryId, final BigDecimal discountRate) {

		this.categoryType = categoryId;
		this.discountRate = discountRate;

	}
	
	/**
	 * @param element
	 * @param fromApiJsonHelper
	 */
	public Map<String, Object> update(final JsonElement element, final FromJsonHelper fromApiJsonHelper) {
		
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		
		final String categoryTypeParamName = "categoryType";
		final String categoryType = fromApiJsonHelper.extractStringNamed("categoryId", element);
		if (StringUtils.isNotBlank(this.categoryType) && !this.categoryType.equals(categoryType)) {
			actualChanges.put(categoryTypeParamName, categoryType);
			this.categoryType = StringUtils.defaultIfEmpty(categoryType, null);
        }
		
		final String discountRateParamName = "discountRate";
		final BigDecimal discountRate = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("discountRate", element);
		if (this.discountRate != null && this.discountRate.compareTo(discountRate) !=0) {
			actualChanges.put(discountRateParamName, discountRate);
			this.discountRate = discountRate;
		}
		
		return actualChanges;
	}


	public void update(DiscountMaster discountMaster) {
		this.discountMaster = discountMaster;
	}

	public void delete() {

		this.isDeleted = "Y";
		this.categoryType = this.getId() + "_" + this.categoryType + "_Y";

	}

	public String getCategoryType() {
		return categoryType;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public DiscountMaster getDiscountMaster() {
		return discountMaster;
	}

	public void setCategoryType(String categoryType) {
		this.categoryType = categoryType;
	}

	public void setDiscountRate(BigDecimal discountRate) {
		this.discountRate = discountRate;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void setDiscountMaster(DiscountMaster discountMaster) {
		this.discountMaster = discountMaster;
	}

}