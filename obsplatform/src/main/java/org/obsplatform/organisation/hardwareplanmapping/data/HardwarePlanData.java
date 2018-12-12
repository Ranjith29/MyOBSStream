package org.obsplatform.organisation.hardwareplanmapping.data;

import java.util.List;

import org.obsplatform.logistics.item.data.ItemData;
import org.obsplatform.portfolio.plan.data.PlanCodeData;

public class HardwarePlanData {

	private Long id;
	private String planCode;
	private String itemCode;

	private List<ItemData> itemDatas;
	private List<PlanCodeData> planDatas;

	public HardwarePlanData(final List<ItemData> itemsdata,
			final List<PlanCodeData> plansData) {

		this.itemDatas = itemsdata;
		this.planDatas = plansData;

	}

	public HardwarePlanData(final Long id, 
			final String planCode, final String itemCode) {

		this.id = id;
		this.planCode = planCode;
		this.itemCode = itemCode;

	}

	public Long getId() {
		return id;
	}

	public String getplanCode() {
		return planCode;
	}

	public String getPlanCode() {
		return planCode;
	}

	public String getItemCode() {
		return itemCode;
	}

	public List<ItemData> getItemDatas() {
		return itemDatas;
	}

	public List<PlanCodeData> getPlanDatas() {
		return planDatas;
	}

	public void addData(List<ItemData> data) {
		this.itemDatas = data;

	}

	public void addPlan(List<PlanCodeData> planData) {

		this.planDatas = planData;
	}

}
