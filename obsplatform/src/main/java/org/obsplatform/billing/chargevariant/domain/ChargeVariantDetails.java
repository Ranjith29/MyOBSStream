package org.obsplatform.billing.chargevariant.domain;


import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.google.gson.JsonElement;

@Entity
@Table(name = "b_chargevariant_detail"/*uniqueConstraints = {@UniqueConstraint(columnNames = {"variant_type","chargevariant_id"},
name = "variantId_with_variantType_uniqueKey")}*/)
public class ChargeVariantDetails extends AbstractPersistable<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "variant_type")
	private String variantType;

	@Column(name = "from_range")
	private Long fromRange;
	
	@Column(name = "to_range")
	private Long toRange;
	
	
	@Column(name="amount_type")
	private String amountType;
	
	@Column(name = "amount")
	private BigDecimal amount;
	

	@ManyToOne
	@JoinColumn(name = "chargevariant_id", insertable = true, updatable = true, nullable = false)
	private ChargeVariant chargeVariant;
	
	@Column(name="is_deleted")
	private char isDeleted = 'N';

	public ChargeVariantDetails() {
	}

	public ChargeVariantDetails(final String variantType,final Long from, final Long to,final String amountType, final BigDecimal amount) {

		this.variantType = variantType;
		this.fromRange = from;
		this.toRange = to;
		this.amountType = amountType;
		this.amount = amount;

	}

	public void update(ChargeVariant chargeVariant) {
		this.chargeVariant = chargeVariant;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getVariantType() {
		return variantType;
	}

	public Long getFrom() {
		return fromRange;
	}

	public Long getTo() {
		return toRange;
	}

	public String getAmountType() {
		return amountType;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public ChargeVariant getChargeVariant() {
		return chargeVariant;
	}

	public char getIsDeleted() {
		return isDeleted;
	}

	public Map<String, Object> update(final JsonElement element, final FromJsonHelper fromApiJsonHelper) {
		
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		
		final String variantTypeParamName = "variantType";
		final String variantType = fromApiJsonHelper.extractStringNamed("variantType", element);
		if (this.differenceExists(this.variantType,variantType)) {
			final String newValue = variantType;
			actualChanges.put(variantTypeParamName, newValue);
			this.variantType = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String fromParamName = "from";
		final Long from = fromApiJsonHelper.extractLongNamed("from", element);
		if (this.differenceExists(this.fromRange, from)) {
			actualChanges.put(fromParamName, from);
			this.fromRange = from;
		}

		final String toParamName = "to";
		final Long to = fromApiJsonHelper.extractLongNamed("to", element);
		if (this.differenceExists(this.toRange, to)) {
			actualChanges.put(toParamName, to);
			this.toRange = to;
		}
		
		final String amountTypeParamName = "amountType";
		final String amountType = fromApiJsonHelper.extractStringNamed("amountType", element);
		if (this.differenceExists(this.amountType, amountType)) {
			final String newValue = amountType;
			actualChanges.put(amountTypeParamName, newValue);
			this.amountType = StringUtils.defaultIfEmpty(newValue, null);
		}

		
		final String amountParamName = "amount";
		final BigDecimal amount = fromApiJsonHelper.extractBigDecimalWithLocaleNamed("amount", element);
		if (this.amount != null && this.amount.compareTo(amount) != 0) {
			actualChanges.put(amountParamName, amount);
			this.amount = amount;
		}

		return actualChanges;

	}

	public void delete() {
                this.isDeleted = 'Y';	
	}
	
	
	 private boolean differenceExists(final String baseValue, final String workingCopyValue) {
	        boolean differenceExists = false;

	        if (StringUtils.isNotBlank(baseValue)) {
	            differenceExists = !baseValue.equals(workingCopyValue);
	        } else {
	            differenceExists = StringUtils.isNotBlank(workingCopyValue);
	        }

	        return differenceExists;
	    }
	 
	 
	 private boolean differenceExists(final Number baseValue, final Number workingCopyValue) {
	        boolean differenceExists = false;

	        if (baseValue != null) {
	            differenceExists = !baseValue.equals(workingCopyValue);
	        } else {
	            differenceExists = workingCopyValue != null;
	        }

	        return differenceExists;
	    }


}