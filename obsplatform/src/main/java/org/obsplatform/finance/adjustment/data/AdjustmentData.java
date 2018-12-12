package org.obsplatform.finance.adjustment.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

public class AdjustmentData {
	
	


	private Long id;
	private Long clientId;
	private LocalDate adjustmentDate;
	private String adjustmentCode;
	private String adjustmentType;
	private BigDecimal amountPaid;
	private Long billId;
	private Long externalId;
	private String remarks;
	
	public AdjustmentData(final Long id, final Long clientId,	final LocalDate adjustmentDate, final String adjustmentCode,
			final BigDecimal amountPaid, final Long billId, final Long externalId, final String remarks) {
		this.id = id;
		this.setClientId(clientId);
		this.setAdjustmentDate(adjustmentDate);
		this.adjustmentCode = adjustmentCode;
		this.setAmountPaid(amountPaid);
		this.setBillId(billId);
		this.setExternalId(externalId);
		this.setRemarks(remarks);
	}

	public AdjustmentData(final Long id, final String adjustmentCode) {

		this.id=id;
		this.adjustmentCode=adjustmentCode;
		
	}
	
	
	public static AdjustmentData instance(final Long id, final Long clientId, final LocalDate adjustmentDate, final String adjustmentCode,
			final BigDecimal amountPaid, final Long billId, final Long externalId, final String remarks){
		
		return new AdjustmentData(id,clientId,adjustmentDate,adjustmentCode,amountPaid,billId,externalId,remarks);
		
	}
	
	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}

	public String getAdjustmentCode() {
		return adjustmentCode;
	}

	public void setAdjustmentCode(final String adjustmentCode) {
		this.adjustmentCode = adjustmentCode;
	}

	public LocalDate getAdjustmentDate() {
		return adjustmentDate;
	}

	public void setAdjustmentDate(final LocalDate adjustmentDate) {
		this.adjustmentDate = adjustmentDate;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(final Long clientId) {
		this.clientId = clientId;
	}

	public String getAdjustmentType() {
		return adjustmentType;
	}

	public void setAdjustmentType(final String adjustmentType) {
		this.adjustmentType = adjustmentType;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(final BigDecimal amountPaid) {
		this.amountPaid = amountPaid;
	}

	public Long getBillId() {
		return billId;
	}

	public void setBillId(final Long billId) {
		this.billId = billId;
	}

	public Long getExternalId() {
		return externalId;
	}

	public void setExternalId(final Long externalId) {
		this.externalId = externalId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(final String remarks) {
		this.remarks = remarks;
	}
	
	

}
