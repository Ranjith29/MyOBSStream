package org.obsplatform.billing.chargevariant.data;

import java.math.BigDecimal;

public class ChargeVariantDetailsData {
	
	private Long id;
	private Long chargeVariantId;
	private String variantType;
	private Long from;
	private Long to;
	private String amountType;
	private BigDecimal amount;

	public ChargeVariantDetailsData(final Long id, final Long chargeVariantId,final String variantType, 
			final Long fromRange, final Long toRange,final String amountType, final BigDecimal amount) {

		this.id = id;
		this.chargeVariantId = chargeVariantId;
		this.variantType = variantType;
		this.from = fromRange;
		this.to = toRange;
		this.amountType = amountType;
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public Long getChargeVariantId() {
		return chargeVariantId;
	}

	public String getVariantType() {
		return variantType;
	}

	public Long getFrom() {
		return from;
	}

	public Long getTo() {
		return to;
	}

	public String getAmountType() {
		return amountType;
	}

	public BigDecimal getAmount() {
		return amount;
	}
	
}
