package org.obsplatform.organisation.partner.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_office_agreement_detail", uniqueConstraints = {@UniqueConstraint(name = "agreement_dtl_ai_ps_mc_uniquekey", columnNames = { "agreement_id", "source" }),
		@UniqueConstraint(name = "serviceid_agreementid_uq", columnNames = { "agreement_id", "service_id"})})

/*, uniqueConstraints = @UniqueConstraint(columnNames = {
		"agreement_id", "source" }, name = "agreement_dtl_ai_ps_mc_uniquekey"))*/
public class AgreementDetails extends AbstractAuditableCustom<AppUser, Long> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "agreement_id", insertable = true, updatable = true, nullable = true, unique = true)
	private Agreement agreements;

	@Column(name = "source")
	private Long sourceType;
	
	@Column(name = "service_id")
	private Long serviceId;

	@Column(name = "share_amount")
	private BigDecimal shareAmount;

	@Column(name = "share_type")
	private String shareType;

	
	@Column(name = "start_date")
	private Date startDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "is_deleted")
	private char isDeleted;

	public AgreementDetails() {

	}

	public AgreementDetails(final Long source, final String shareType, final BigDecimal shareAmount,  final LocalDate startDate,
			final LocalDate endDate, final Long serviceId) {
		
		this.sourceType = source;
		this.shareType = shareType;
		this.shareAmount =shareAmount;
		this.startDate = startDate.toDate();
		this.isDeleted = 'N';
		if(endDate !=null)
		this.endDate = endDate.toDate();
		this.serviceId = serviceId;
		
	}

	public Agreement getAgreements() {
		return agreements;
	}

	public Long getSourceType() {
		return sourceType;
	}

	public BigDecimal getShareAmount() {
		return shareAmount;
	}

	public String getShareType() {
		return shareType;
	}


	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public char getIsDeleted() {
		return isDeleted;
	}
	

	public void setSourceType(Long sourceType) {
		this.sourceType = sourceType;
	}

	public void setShareAmount(BigDecimal shareAmount) {
		this.shareAmount = shareAmount;
	}

	public void setShareType(String shareType) {
		this.shareType = shareType;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setIsDeleted(char isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public void update (final Agreement agreement){
		this.agreements = agreement;
	}

	public void setServiceId(Long serviceId) {
		this.serviceId = serviceId;
	}

	
}
