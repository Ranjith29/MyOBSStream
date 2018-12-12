package org.obsplatform.scheduledjobs.dataupload.service;

import org.obsplatform.crm.clientprospect.service.SearchSqlQuery;
import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.scheduledjobs.dataupload.data.UploadStatusData;

public interface DataUploadReadPlatformService {

    Page<UploadStatusData> retrieveAllUploadStatusData(SearchSqlQuery searchUploads);
}
