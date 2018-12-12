package org.obsplatform.billing.chargevariant.data;

import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;

public class ChargeVariantData {
	
	private List<EnumOptionData> statusData;
	private Collection<MCodeData> chargeVariantTypeData;
	private Long id;
	private String chargeVariantCode;
	private String status;
	private LocalDate startDate;
	private LocalDate endDate;
	private LocalDate date;
	private List<ChargeVariantDetailsData> chargeVariantDetailsDatas;
	private Collection<MCodeData> amountTypeData;

	public ChargeVariantData(List<EnumOptionData> statusData, Collection<MCodeData> amountTypeData, Collection<MCodeData> chargeVariantTypeData) {

	             this.statusData= statusData;
	             this.amountTypeData = amountTypeData;
	             this.chargeVariantTypeData = chargeVariantTypeData;
	             this.date= DateUtils.getLocalDateOfTenant();
	}

	public ChargeVariantData(Long id, String chargeVariantCode, LocalDate startDate, LocalDate endDate, String status) {
			
		   this.id = id;
		   this.chargeVariantCode = chargeVariantCode;
		   this.startDate = startDate;
		   this.endDate = endDate;
		   this.status = status;
		
	}

	public void setChargeVariantDetailsData(List<ChargeVariantDetailsData> chargeVariantDetailsDatas) {

		  this.chargeVariantDetailsDatas = chargeVariantDetailsDatas;
	}

	public void setStatusData(List<EnumOptionData> statusData) {

		  this.statusData = statusData;
	}

	public void setVariantTypeData(Collection<MCodeData> chargeVariantTypeData) {
                   this.chargeVariantTypeData = chargeVariantTypeData;
	}

	public Collection<MCodeData> getAmountTypeData() {
		return amountTypeData;
	}

	public void setAmountTypeData(Collection<MCodeData> amountTypeData) {
		this.amountTypeData = amountTypeData;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

}
