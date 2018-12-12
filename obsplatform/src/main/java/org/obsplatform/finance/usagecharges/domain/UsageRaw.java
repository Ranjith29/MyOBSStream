
package org.obsplatform.finance.usagecharges.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.useradministration.domain.AppUser;

/**
 * @author Ranjith
 *
 */

@Entity
@Table(name="b_usage_raw")
public class UsageRaw extends AbstractAuditableCustom<AppUser,Long>{ 
	
	private static final long serialVersionUID = 1L;

	@Column(name="client_id")
	private Long clientId;
	
	@Column(name="number")
	private String number;
	
	@Column(name="time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date time;
	
	@Column(name="destination_number")
	private String destinationNumber;
	
	@Column(name="destination_location")
	private String destinationLocation;
	
	@Column(name="duration")
	private BigDecimal duration;
	
	@Column(name="cost")
	private BigDecimal cost;
	
	@ManyToOne
	@JoinColumn(name = "usage_charge_id", insertable = true, updatable = true, nullable = true)
	private UsageCharge usageCharge;

	@Column(name="internal_id", unique = true)
	private Long internalId;
	
	
	public UsageRaw() {
		
	}

	public UsageRaw(Long clientId,String number,String time,String destinationNumber,String destinationLocation,
			BigDecimal duration, BigDecimal cost, Long internalId) {
		
		this.clientId = clientId;
		this.number = number;
		this.time = DateUtils.parseLocalDateTime(time,"dd/MM/yyyy HH:mm:ss").toDate();
		this.destinationNumber = destinationNumber;
		this.destinationLocation = destinationLocation;
		this.duration = duration;
		this.cost = cost;
		this.internalId=internalId;
		
	}

	/**
	 * @param command
	 * @return
	 */
	public static UsageRaw fromJson(final JsonCommand command) {
		
		final Long clientId = command.longValueOfParameterNamed("clientId");
		final String number = command.stringValueOfParameterNamed("number");
		final String time = command.stringValueOfParameterNamed("time");
		final String destinationNumber = command.stringValueOfParameterNamed("destinationNumber");
		final String destinationLocation = command.stringValueOfParameterNamed("destinationLocation");
		final BigDecimal duration = command.bigDecimalValueOfParameterNamed("duration");
		final BigDecimal cost = command.bigDecimalValueOfParameterNamed("cost");
		final Long internalId =command.longValueOfParameterNamed("internalId");
		
		return new UsageRaw(clientId,number,time,destinationNumber,destinationLocation,duration,cost,internalId);
	}

	public Long getClientId() {
		return clientId;
	}

	public String getNumber() {
		return number;
	}

	public Date getTime() {
		return time;
	}

	public String getDestinationNumber() {
		return destinationNumber;
	}

	public String getDestinationLocation() {
		return destinationLocation;
	}

	public BigDecimal getDuration() {
		return duration;
	}

	public BigDecimal getCost() {
		return cost;
	}

	public void update(UsageCharge usageCharge) {

		this.usageCharge = usageCharge;
	}
	public Long getInternalId() {
		return internalId;
	}

	public void setInternalId(Long internalId) {
		this.internalId = internalId;
	}



}
