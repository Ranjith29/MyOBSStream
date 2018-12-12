package org.obsplatform.scheduledjobs.scheduledjobs.data;

import java.util.List;

public class ScheduleJobData {
	
	private Long id;
	private String batchName;
	private String query;
	private List<ScheduleJobData> jobDetailData;

	public ScheduleJobData(Long id, String batchName, String query) {
		this.id = id;
		this.batchName = batchName;
		this.query = query;

	}

	public ScheduleJobData(final List<ScheduleJobData> jobDetailData) {
		this.jobDetailData = jobDetailData;
	
	}

	public Long getId() {
		return id;
	}

	public String getBatchName() {
		return batchName;
	}

	public String getQuery() {
		return query;
	}

	public List<ScheduleJobData> getJobDetailData() {
		return jobDetailData;
	}

}
