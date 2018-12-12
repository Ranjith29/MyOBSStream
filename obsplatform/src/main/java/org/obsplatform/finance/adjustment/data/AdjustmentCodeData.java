package org.obsplatform.finance.adjustment.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.billing.discountmaster.data.DiscountValues;

public class AdjustmentCodeData {

	private final List<AdjustmentData> data;
	private List<DiscountValues> discountOptions;
	private LocalDate date;

	public AdjustmentCodeData(final List<AdjustmentData> data) {
		this.data=data;
		this.discountOptions=setadjustment_type();
	}

	public List<AdjustmentData> getData() {
		return data;
	}

	public List<DiscountValues> setadjustment_type() {

		discountOptions = new ArrayList<DiscountValues>();
		discountOptions.add(new DiscountValues("CREDIT"));
		discountOptions.add(new DiscountValues("DEBIT"));

		return discountOptions;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	
}
