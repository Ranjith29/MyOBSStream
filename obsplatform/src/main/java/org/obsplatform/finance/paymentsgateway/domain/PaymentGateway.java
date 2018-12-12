package org.obsplatform.finance.paymentsgateway.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * 
 * @author ashokreddy
 *
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "b_paymentgateway")
public class PaymentGateway extends AbstractAuditableCustom<AppUser, Long> {
	
	@Column(name = "key_id")
	private String deviceId;

	@Column(name="party_id")
	private String partyId;
	
	@Column(name = "payment_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date paymentDate;
	
	@Column(name = "amount_paid", scale = 6, precision = 19, nullable = false)
	private BigDecimal amountPaid;
	
	@Column(name = "receipt_no")
	private String receiptNo;
	
	@Column(name = "source")
	private String source;

	@Column(name="t_details")
	private String details;
	
	@Column(name = "payment_id")
	private String paymentId;
	
	@Column(name = "obs_id")
	private Long obsId;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "Remarks")
	private String remarks;
	
	@Column(name = "t_status")
	private String tStatus;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "is_auto" ,nullable = false)
	private boolean isAuto=true;
	
	@Column(name = "reprocess_detail")
	private String reProcessDetail;
	
	
	public PaymentGateway(){
		
	}
	
	/*public PaymentGateway(final String deviceId, final String partyId, final Date paymentDate,
			final BigDecimal amountPaid, final String receiptNo,
			final String source, final String details) {
		
		1//this.deviceId=deviceId;
		4//this.partyId=partyId;
		8//this.paymentDate=paymentDate;
		3//this.amountPaid=amountPaid;
		2//this.receiptNo=receiptNo;
		9//this.source=source;
		7//this.details=details;
	}*/
	
	public static PaymentGateway getPaymentGateway(final String deviceId, final String partyId, final Date paymentDate,
			final BigDecimal amountPaid, final String receiptNo, final String source, final String details) {	
		return new PaymentGateway(deviceId, receiptNo, amountPaid, partyId, null, null, details, paymentDate, source);
	}

	public PaymentGateway(final String deviceId, final String transactionId,
			final BigDecimal amountPaid, final String partyId, final String type,
			 final String tStatus, final String details, final Date date, final String source) {
		this.deviceId=deviceId;
		this.partyId=partyId;
		this.paymentDate=date;
		this.amountPaid=amountPaid;
		this.receiptNo=transactionId;
		this.source=source;
		this.details=details;
		this.type=type;
		this.tStatus=tStatus;
	}

	public Map<String, Object> fromJson(final JsonCommand command) {
		 
		final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(1);
		 final String remarks = "remarks";
		 if (command.isChangeInStringParameterNamed(remarks,this.remarks)) {
				final String newValue = command.stringValueOfParameterNamed("remarks");
				actualChanges.put(remarks, newValue);
				this.remarks = StringUtils.defaultIfEmpty(newValue, null);
		 }
		 final String status = "status";
			if (command.isChangeInStringParameterNamed(status,
					this.status)) {
				final String newValue = command.stringValueOfParameterNamed("status");
				actualChanges.put(status, newValue);
				this.status = StringUtils.defaultIfEmpty(newValue, null);
			}
			return actualChanges;	 
		 
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getPartyId() {
		return partyId;
	}

	public Date getPaymentDate() {
		return paymentDate;
	}

	public BigDecimal getAmountPaid() {
		return amountPaid;
	}

	public String getReceiptNo() {
		return receiptNo;
	}

	public String getSource() {
		return source;
	}

	public String getDetails() {
		return details;
	}

	public String getPaymentId() {
		return paymentId;
	}

	public Long getObsId() {
		return obsId;
	}

	public String getStatus() {
		return status;
	}

	public void setObsId(Long value) {
		this.obsId = value;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setAuto(boolean isAuto) {
		this.isAuto = isAuto;
	}

	public boolean isAuto() {
		return isAuto;
	}

	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(Object remarks) {
		this.remarks = (String)remarks;
	}

	public String getReProcessDetail() {
		return reProcessDetail;
	}

	public void setReProcessDetail(String reProcessDetail) {
		this.reProcessDetail = reProcessDetail;
	}
	
}
