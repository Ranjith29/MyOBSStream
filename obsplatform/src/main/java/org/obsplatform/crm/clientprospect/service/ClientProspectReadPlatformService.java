package org.obsplatform.crm.clientprospect.service;

import java.util.Collection;
import java.util.List;

import org.obsplatform.crm.clientprospect.data.ClientProspectCardDetailsData;
import org.obsplatform.crm.clientprospect.data.ClientProspectData;
import org.obsplatform.crm.clientprospect.data.ProspectDetailAssignedToData;
import org.obsplatform.crm.clientprospect.data.ProspectDetailData;
import org.obsplatform.crm.clientprospect.data.ProspectPlanCodeData;
import org.obsplatform.infrastructure.core.service.Page;

public interface ClientProspectReadPlatformService {

	public Collection<ClientProspectData> retriveClientProspect();

	public ProspectDetailData retriveClientProspect(Long clientProspectId);

	public Collection<ProspectPlanCodeData> retrivePlans();

	public List<ProspectDetailAssignedToData> retrieveUsers();

	public List<ProspectDetailData> retriveProspectDetailHistory(Long prospectdetailid, Long userId);

	public ClientProspectData retriveSingleClient(Long id, Long userId);

	public Page<ClientProspectData> retriveClientProspect(SearchSqlQuery searchClientProspect, Long userId);
	
	public ProspectPlanCodeData retriveClientProspectOrder(Long clientProspectId);
	
	public ClientProspectCardDetailsData retriveclientProspectCardDetailsData(Long prospectId);

	public String getFileName(Long prospectId);
}
