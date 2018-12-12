package org.obsplatform.infrastructure.jobs.service;

import java.util.List;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.obsplatform.infrastructure.jobs.domain.ScheduledJobRunHistory;
import org.obsplatform.infrastructure.jobs.domain.SchedulerDetail;

public interface SchedularWritePlatformService {

    public List<ScheduledJobDetail> retrieveAllJobs();

    public ScheduledJobDetail findByJobKey(String triggerKey);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails);

    public void saveOrUpdate(ScheduledJobDetail scheduledJobDetails, ScheduledJobRunHistory scheduledJobRunHistory);

    public Long fetchMaxVersionBy(String triggerKey);

    public ScheduledJobDetail findByJobId(Long jobId);

    public CommandProcessingResult updateJobDetail(Long jobId, JsonCommand command);

    public SchedulerDetail retriveSchedulerDetail();

    public void updateSchedulerDetail(final SchedulerDetail schedulerDetail);

    public boolean processJobDetailForExecution(String jobKey, String triggerType);

	public CommandProcessingResult updateJobParametersDetail(Long entityId,JsonCommand command);

}
