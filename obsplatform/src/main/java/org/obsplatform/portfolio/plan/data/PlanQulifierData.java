package org.obsplatform.portfolio.plan.data;

import java.util.List;

import org.obsplatform.organisation.partner.data.PartnersData;

public class PlanQulifierData {
	private final List<PartnersData> partnersDatas;
	private final List<PartnersData> availabePartnersDatas;

	public PlanQulifierData(List<PartnersData> partnersDatas,List<PartnersData> availabePartnersDatas) {

	       this.partnersDatas = partnersDatas;
	       this.availabePartnersDatas = availabePartnersDatas;
	}

}
