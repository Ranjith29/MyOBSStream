package org.obsplatform.provisioning.processscheduledjobs.service;

import java.util.List;

import org.joda.time.LocalDate;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EvoBatchData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.ScheduleJobData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.WorldpayBatchData;

public interface SheduleJobReadPlatformService {

	//List<ScheduleJobData> retrieveSheduleJobDetails();

	List<Long> getClientIds(String query, JobParameterData data);

	Long getMessageId(String processParam);

	List<ScheduleJobData> retrieveSheduleJobParameterDetails(String paramValue);

	JobParameterData getJobParameters(String jobName);

	List<ScheduleJobData> getJobQeryData();

	String retrieveMessageData(Long id);
	
	List<ScheduleJobData> retrieveSheduleJobDetails(String paramValue);

	List<Long> getBillIds(String query, JobParameterData data);

	List<Long> retrieveAddonsForDisconnection(LocalDate processingDate);

	List<UsageChargesData> getCustomerUsageRawData(String query,JobParameterData data);

	List<EvoBatchData> getUnProcessedRecurringData();
	
	List<Long> getClientIds(String query);
	
	List<Long> getTicketIds(String query);
	
	List<String> getClientData(String query);
	
	//for worldpay 
	List<WorldpayBatchData> getUnProcessedWorldPayRecurringData();
		
	
}
