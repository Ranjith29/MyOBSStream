package org.obsplatform.billing.chargevariant.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * @author hugo
 * 
 */
@Entity
@Table(name = "b_chargevariant", uniqueConstraints = @UniqueConstraint(name = "chargevariantcode", columnNames = { "chargevariant_code" }))
public class ChargeVariant extends AbstractAuditableCustom<AppUser,Long> {

	private static final long serialVersionUID = 1L;

	@Column(name = "chargevariant_code")
	private String chargevariantCode;

	@Column(name = "status")
	private String status;


	@Column(name = "start_date")
	private Date startDate;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "is_delete")
	private char isDelete = 'N';
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "chargeVariant", orphanRemoval = true)
	private List<ChargeVariantDetails> chargeVariantDetails = new ArrayList<ChargeVariantDetails>();

	public ChargeVariant() {

	}

	public ChargeVariant(String chargeVariantCode, String status, LocalDate startDate, LocalDate endDate) {
	        
		this.chargevariantCode = chargeVariantCode;
		this.status = status;
		this.startDate = startDate.toDate();
		this.endDate = endDate.toDate();
	}
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}


	public static ChargeVariant fromJson(final JsonCommand command) {
		
		final String chargeVariantCode = command.stringValueOfParameterNamed("chargeVariantCode");
		final String status = command.stringValueOfParameterNamed("status");
		final LocalDate startDate = command.localDateValueOfParameterNamed("startDate");
		final LocalDate endDate = command.localDateValueOfParameterNamed("endDate");

		return new ChargeVariant(chargeVariantCode, status,startDate,endDate);
	}

	/**
	 * @param command
	 * @return changes of discountmaster object
	 */
	public Map<String, Object> update(final JsonCommand command) {
		
		final Map<String, Object> actualChanges = new ConcurrentHashMap<String, Object>(1);
		
		final String chargeVariantCodeParamName = "chargeVariantCode";
		if (command.isChangeInStringParameterNamed(chargeVariantCodeParamName, this.chargevariantCode)) {
			final String newValue = command.stringValueOfParameterNamed(chargeVariantCodeParamName);
			actualChanges.put(chargeVariantCodeParamName, newValue);
			this.chargevariantCode = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String statusParamName = "status";
		if (command.isChangeInStringParameterNamed(statusParamName,this.status)) {
			final String newValue = command.stringValueOfParameterNamed(statusParamName);
			actualChanges.put(statusParamName, newValue);
			this.status = StringUtils.defaultIfEmpty(newValue, null);
		}

		final String startDateParamName = "startDate";
		if (command.isChangeInLocalDateParameterNamed(startDateParamName,new LocalDate(this.startDate))) {
			final LocalDate newValue = command.localDateValueOfParameterNamed(startDateParamName);
			actualChanges.put(startDateParamName, newValue);
			this.startDate = newValue.toDate();
		}

		final String endDateParamName = "endDate";
		if (command.isChangeInLocalDateParameterNamed(startDateParamName,new LocalDate(this.endDate))) {
			final LocalDate newValue = command.localDateValueOfParameterNamed(endDateParamName);
			actualChanges.put(endDateParamName, newValue);
			this.endDate = newValue.toDate();
		}


		return actualChanges;

	}

	/**
	 * updating column 'is_deleted' with 'Y' for delete of chargeVariant
	 */
	public void delete() {

		if (this.isDelete == 'N') {
			this.isDelete = 'Y';
			this.chargevariantCode = this.chargevariantCode + "_" + this.getId();
			for(ChargeVariantDetails chargeVariantDetails:this.chargeVariantDetails){
				chargeVariantDetails.delete();
			}
		}
	}

	public void addDetails(ChargeVariantDetails chargeVariantDetails) {
		chargeVariantDetails.update(this);
		this.chargeVariantDetails.add(chargeVariantDetails);

	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getChargevariantCode() {
		return chargevariantCode;
	}

	public String getStatus() {
		return status;
	}

	public Date getEndDate() {
		return endDate;
	}

	public List<ChargeVariantDetails> getChargeVariantDetails() {
		return chargeVariantDetails;
	}

	public char getIsDelete() {
		return isDelete;
	}

}
