package org.obsplatform.infrastructure.jobs.service;

import java.util.List;

import org.obsplatform.infrastructure.core.service.Page;
import org.obsplatform.infrastructure.jobs.data.JobDetailData;
import org.obsplatform.infrastructure.jobs.data.JobDetailHistoryData;
import org.obsplatform.portfolio.group.service.SearchParameters;
import org.obsplatform.scheduledjobs.scheduledjobs.data.ScheduleJobData;

public interface SchedulerJobRunnerReadService {

    public List<JobDetailData> findAllJobDeatils();

    public JobDetailData retrieveOne(Long jobId);

    public Page<JobDetailHistoryData> retrieveJobHistory(Long jobId, SearchParameters searchParameters);

    public boolean isUpdatesAllowed();

	public List<ScheduleJobData> retrieveJobDetails();

	

}
