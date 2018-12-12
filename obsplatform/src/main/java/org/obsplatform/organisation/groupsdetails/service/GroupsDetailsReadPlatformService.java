package org.obsplatform.organisation.groupsdetails.service;

import java.util.List;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.groupsdetails.data.GroupsDetailsData;

public interface GroupsDetailsReadPlatformService {

	Page<GroupsDetailsData> retrieveAllGroupsData(SearchSqlQuery searchGroupsDetails);

	List<Long> retrieveclientIdsByGroupId(Long groupId);

}
