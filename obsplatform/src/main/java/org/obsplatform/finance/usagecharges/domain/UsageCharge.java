package org.obsplatform.finance.usagecharges.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * @author Ranjith
 * 
 */
@Entity
@Table(name = "b_usage_charge")
public class UsageCharge extends AbstractAuditableCustom<AppUser, Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "client_id")
	private Long clientId;
	
	@Column(name = "number")
	private String number;

	@Column(name = "charge_date")
	@Temporal(TemporalType.TIMESTAMP)
	private Date chargeDate;

	@Column(name = "total_duration")
	private BigDecimal totalDuration;

	@Column(name = "total_cost")
	private BigDecimal totalCost;
	
	@Column(name = "charge_id")
	private Long chargeId;
	
	//Here CascadeType set to merge only bcz handling of JPA/Hibernate: 
	//detached entity passed to persist execption(persistenceexception)
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade=CascadeType.MERGE, mappedBy = "usageCharge", orphanRemoval = true)  
	private List<UsageRaw> usageRawData = new ArrayList<UsageRaw>();


	public UsageCharge() {

	}

	public UsageCharge(Long clientId,String number, DateTime date, BigDecimal totalCost,BigDecimal totalDuration) {

		this.clientId = clientId;
		this.number = number;
		this.chargeDate = date.toDate();
		this.totalCost = totalCost;
		this.totalDuration = totalDuration;

	}

	public Long getClientId() {
		return clientId;
	}

	public String getNumber() {
		return number;
	}

	public LocalDate getChargeDate() {
		return new LocalDate(chargeDate);
	}

	public BigDecimal getTotalDuration() {
		return totalDuration;
	}

	public void setTotalDuration(BigDecimal totalDuration) {

		this.totalDuration = totalDuration;

	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {

		this.totalCost = totalCost;
	}

	public List<UsageRaw> getUsageRawData() {
		return usageRawData;
	}

	public void addUsageRaw(UsageRaw usageRawData) {
		usageRawData.update(this);
		this.usageRawData.add(usageRawData);

	}

	public void setChargeId(Long chargeId) {

		this.chargeId = chargeId;

	}

}
