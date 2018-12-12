package org.obsplatform.portfolio.search.service;

import java.util.Collection;

import org.obsplatform.portfolio.search.data.SearchConditions;
import org.obsplatform.portfolio.search.data.SearchData;

public interface SearchReadPlatformService {

    Collection<SearchData> retriveMatchingData(SearchConditions searchConditions);
}
