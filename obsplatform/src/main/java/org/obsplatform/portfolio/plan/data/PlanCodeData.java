package org.obsplatform.portfolio.plan.data;

import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.service.DateUtils;

public class PlanCodeData {
	private final Long id;
	private final String planCode;
	private final List<ServiceData> availableServices;
	private final LocalDate starDate;
	private final String isPrepaid;
	private final String planDescription;
	
	public PlanCodeData(final Long id,final String planCode,final List<ServiceData> data,final String isPrepaid,
			final String planDescription)
	{
		this.id=id;
		this.planCode=planCode;
		this.availableServices=data;
		this.starDate=DateUtils.getLocalDateOfTenant();
		this.isPrepaid=isPrepaid;
		this.planDescription=planDescription;

	}

	public Long getId() {
		return id;
	}

	public String getPlanCode() {
		return planCode;
	}

	public List<ServiceData> getData() {
		return availableServices;
	}

	public LocalDate getStartDate() {
		return starDate;
	}


	public List<ServiceData> getAvailableServices() {
		return availableServices;
	}

	public LocalDate getStart_date() {
		return starDate;
	}

	
	public String getIsPrepaid() {
		return isPrepaid;
	}


}
