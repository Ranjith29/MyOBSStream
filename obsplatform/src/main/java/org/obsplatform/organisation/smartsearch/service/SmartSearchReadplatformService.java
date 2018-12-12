package org.obsplatform.organisation.smartsearch.service;

import java.util.Date;

import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.smartsearch.data.SmartSearchData;

public interface SmartSearchReadplatformService {

	Page<SmartSearchData> retrieveAllSearchData(String searchText,Date fromDate, Date toDate,
			Integer limit, Integer offset);

}
