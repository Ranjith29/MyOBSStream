package org.obsplatform.organisation.smartsearch.service;

import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.organisation.smartsearch.data.AdvanceSearchData;
import org.obsplatform.portfolio.group.service.SearchParameters;

public interface AdvanceSearchReadPlafformService {

	Page<AdvanceSearchData> retrieveAllSearchData(SearchParameters searchParameters);

}
