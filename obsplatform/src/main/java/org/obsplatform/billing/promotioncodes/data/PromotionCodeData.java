package org.obsplatform.billing.promotioncodes.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.portfolio.contract.data.PeriodData;

/**
 * @author hugo
 * 
 */
public class PromotionCodeData {

	private Long id;
	private String promotionCode;
	private String promotionDescription;
	private String durationType;
	private Long duration;
	private String discountType;
	private LocalDate startDate;
	private BigDecimal discountRate;
	private Collection<MCodeData> discountTypeData;
	private List<PeriodData> contractTypedata;
	private LocalDate date;

	public PromotionCodeData(final Long id, final String promotionCode,
			final String promotionDescription, final String durationType, final Long duration,
			final String discountType, final BigDecimal discountRate, final LocalDate startDate) {

		this.id = id;
		this.promotionCode = promotionCode;
		this.promotionDescription = promotionDescription;
		this.durationType = durationType;
		this.duration = duration;
		this.discountType = discountType;
		this.discountRate = discountRate;
		this.startDate = startDate;
	}

	public PromotionCodeData() { 
		
	}
	
	public PromotionCodeData(final Collection<MCodeData> discountTypeData,
			final List<PeriodData> contractTypedata) {

		this.discountTypeData = discountTypeData;
		this.contractTypedata = contractTypedata;

	}

	public Long getId() {
		return id;
	}

	public String getPromotionCode() {
		return promotionCode;
	}

	public String getPromotionDescription() {
		return promotionDescription;
	}

	public String getDurationType() {
		return durationType;
	}

	public Long getDuration() {
		return duration;
	}

	public String getDiscountType() {
		return discountType;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public BigDecimal getDiscountRate() {
		return discountRate;
	}

	public Collection<MCodeData> getDiscountTypeData() {
		return discountTypeData;
	}

	public void setDiscounTypeData(final Collection<MCodeData> discountTypeData) {
		this.discountTypeData = discountTypeData;
	}

	public List<PeriodData> getContractTypedata() {
		return contractTypedata;
	}

	public void setContractTypedata(final List<PeriodData> contractTypedata) {
		this.contractTypedata = contractTypedata;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
	

}
