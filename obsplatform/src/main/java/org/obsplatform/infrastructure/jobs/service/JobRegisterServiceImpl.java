package org.obsplatform.infrastructure.jobs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.exception.PlatformInternalServerException;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.infrastructure.jobs.annotation.CronMethodParser;
import org.obsplatform.infrastructure.jobs.domain.ScheduledJobDetail;
import org.obsplatform.infrastructure.jobs.domain.SchedulerDetail;
import org.obsplatform.infrastructure.jobs.exception.JobNotFoundException;
import org.obsplatform.infrastructure.security.service.TenantDetailsService;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * Service class to create and load batch jobs to Scheduler using
 * {@link SchedulerFactoryBean} ,{@link MethodInvokingJobDetailFactoryBean} and
 * {@link CronTriggerFactoryBean}
 * 
 */
@Service
public class JobRegisterServiceImpl implements JobRegisterService,ApplicationListener<ContextClosedEvent> {

    private final static Logger logger = LoggerFactory.getLogger(JobRegisterServiceImpl.class);

    private ApplicationContext applicationContext;
    private SchedularWritePlatformService schedularWritePlatformService;
    private TenantDetailsService tenantDetailsService;
    private SchedulerJobListener schedulerJobListener;
    private SchedulerStopListener schedulerStopListener;
    private SchedulerTriggerListener globalSchedulerTriggerListener;

    private final HashMap<String, Scheduler> schedulers = new HashMap<String, Scheduler>(4);
    
    //This class cannot use constructor injection, because one of
    // its dependencies (SchedulerStopListener) has a circular dependency to
    // itself. So, slightly differently from how it's done elsewhere in this
    // code base, the following fields are not final, and there is no
    // constructor, but setters.
    
    @Autowired
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setSchedularWritePlatformService(SchedularWritePlatformService schedularWritePlatformService) {
        this.schedularWritePlatformService = schedularWritePlatformService;
    }

    @Autowired
    public void setTenantDetailsService(TenantDetailsService tenantDetailsService) {
        this.tenantDetailsService = tenantDetailsService;
    }

    @Autowired
    public void setSchedulerJobListener(SchedulerJobListener schedulerJobListener) {
        this.schedulerJobListener = schedulerJobListener;
    }

    @Autowired
    public void setSchedulerStopListener(SchedulerStopListener schedulerStopListener) {
        this.schedulerStopListener = schedulerStopListener;
    }

    @Autowired
    public void setGlobalTriggerListener(SchedulerTriggerListener globalTriggerListener) {
        this.globalSchedulerTriggerListener = globalTriggerListener;
    }

    @PostConstruct
    public void loadAllJobs() {
        List<ObsPlatformTenant> allTenants = this.tenantDetailsService.findAllTenants();
        for (ObsPlatformTenant tenant : allTenants) {
            ThreadLocalContextUtil.setTenant(tenant);
            List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService.retrieveAllJobs();
            for (ScheduledJobDetail jobDetails : scheduledJobDetails) {
                scheduleJob(jobDetails);
                jobDetails.updateTriggerMisfired(false);
                this.schedularWritePlatformService.saveOrUpdate(jobDetails);
            }
            SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
            if (schedulerDetail.isResetSchedulerOnBootup()) {
                schedulerDetail.updateSuspendedState(false);
                this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            }
        }
    }

    public void executeJob(final ScheduledJobDetail scheduledJobDetail, String triggerType) {
        try {
            final JobDataMap jobDataMap = new JobDataMap();
            if (triggerType == null) {
                triggerType = SchedulerServiceConstants.TRIGGER_TYPE_APPLICATION;
            }
            jobDataMap.put(SchedulerServiceConstants.TRIGGER_TYPE_REFERENCE, triggerType);
            jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, ThreadLocalContextUtil.getTenant().getTenantIdentifier());
            final String key = scheduledJobDetail.getJobKey();
            final JobKey jobKey = constructJobKey(key);
            final String schedulerName = getSchedulerName(scheduledJobDetail);
            final Scheduler scheduler = schedulers.get(schedulerName);
            if (scheduler == null || !scheduler.checkExists(jobKey)) {
            	final JobDetail jobDetail = createJobDetail(scheduledJobDetail);
            	final String tempSchedulerName = "temp" + scheduledJobDetail.getId();
            	final Scheduler tempScheduler = createScheduler(tempSchedulerName, 1, schedulerJobListener, schedulerStopListener);
                tempScheduler.addJob(jobDetail, true);
                jobDataMap.put(SchedulerServiceConstants.SCHEDULER_NAME, tempSchedulerName);
                this.schedulers.put(tempSchedulerName, tempScheduler);
                tempScheduler.triggerJob(jobDetail.getKey(), jobDataMap);
            } else {
                scheduler.triggerJob(jobKey, jobDataMap);
            }

        } catch (Exception e) {
            throw new PlatformInternalServerException("error.msg.sheduler.job.execution.failed", "Job execution failed for job with id:"
                    + scheduledJobDetail.getId(), scheduledJobDetail.getId());
        }

    }

    public void rescheduleJob(final ScheduledJobDetail scheduledJobDetail) {
        try {
            String jobIdentity = scheduledJobDetail.getJobKey();
            JobKey jobKey = constructJobKey(jobIdentity);
            String schedulername = getSchedulerName(scheduledJobDetail);
            Scheduler scheduler = schedulers.get(schedulername);
            if (scheduler != null) {
                scheduler.deleteJob(jobKey);
            }
            scheduleJob(scheduledJobDetail);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        } catch (Throwable throwable) {
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetail.updateErrorLog(stackTrace);
            this.schedularWritePlatformService.saveOrUpdate(scheduledJobDetail);
        }
    }

    @Override
    public void pauseScheduler() {
        SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (!schedulerDetail.isSuspended()) {
            schedulerDetail.updateSuspendedState(true);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
        }
    }

    @Override
    public void startScheduler() {
        SchedulerDetail schedulerDetail = this.schedularWritePlatformService.retriveSchedulerDetail();
        if (schedulerDetail.isSuspended()) {
            schedulerDetail.updateSuspendedState(false);
            this.schedularWritePlatformService.updateSchedulerDetail(schedulerDetail);
            if (schedulerDetail.isExecuteInstructionForMisfiredJobs()) {
                List<ScheduledJobDetail> scheduledJobDetails = this.schedularWritePlatformService.retrieveAllJobs();
                for (ScheduledJobDetail jobDetail : scheduledJobDetails) {
                    if (jobDetail.isTriggerMisfired()) {
                        if (jobDetail.isActiveSchedular()) {
                            executeJob(jobDetail, SchedulerServiceConstants.TRIGGER_TYPE_CRON);
                        }
                        String schedulerName = getSchedulerName(jobDetail);
                        Scheduler scheduler = schedulers.get(schedulerName);
                        if (scheduler != null) {
                            String key = jobDetail.getJobKey();
                            JobKey jobKey = constructJobKey(key);
                            try {
                                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                                for (Trigger trigger : triggers) {
                                    if (trigger.getNextFireTime() != null && trigger.getNextFireTime().after(jobDetail.getNextRunTime())) {
                                        jobDetail.updateNextRunTime(trigger.getNextFireTime());
                                    }
                                }
                            } catch (SchedulerException e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        jobDetail.updateTriggerMisfired(false);
                        this.schedularWritePlatformService.saveOrUpdate(jobDetail);
                    }
                }
            }
        }
    }

    @Override
    public void rescheduleJob(final Long jobId) {
        ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        rescheduleJob(scheduledJobDetail);
    }

    @Override
    public void executeJob(final Long jobId) {
        ScheduledJobDetail scheduledJobDetail = this.schedularWritePlatformService.findByJobId(jobId);
        if (scheduledJobDetail == null) { throw new JobNotFoundException(String.valueOf(jobId)); }
        executeJob(scheduledJobDetail, null);
    }

    @Override
    public boolean isSchedulerRunning() {
        return !this.schedularWritePlatformService.retriveSchedulerDetail().isSuspended();
    }

    /**
     * Need to use ContextClosedEvent instead of ContextStoppedEvent because in
     * case Spring Boot fails to start-up (e.g. because Tomcat port is already
     * in use) then org.springframework.boot.SpringApplication.run(String...)
     * does a context.close(); and not a context.stop();
     */
	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		this.stopAllSchedulers();
	}

	@Override
	public void stopAllSchedulers() {
	    for (Scheduler scheduler : this.schedulers.values()) {
            try {
                scheduler.shutdown();
            } catch (final SchedulerException e) {
                logger.error(e.getMessage(), e);
            }
        }
	}
    
    private void scheduleJob(final ScheduledJobDetail scheduledJobDetails) {
    	
        if (!scheduledJobDetails.isActiveSchedular()) {
            scheduledJobDetails.updateNextRunTime(null);
            scheduledJobDetails.updateCurrentlyRunningStatus(false);
            return;
        }
        try {
            JobDetail jobDetail = createJobDetail(scheduledJobDetails);
            Trigger trigger = createTrigger(scheduledJobDetails, jobDetail);
            Scheduler scheduler = getScheduler(scheduledJobDetails);
            scheduler.scheduleJob(jobDetail, trigger);
            scheduledJobDetails.updateJobKey(getJobKeyAsString(jobDetail.getKey()));
            scheduledJobDetails.updateNextRunTime(trigger.getNextFireTime());
            scheduledJobDetails.updateErrorLog(null);
        } catch (Throwable throwable) {
            scheduledJobDetails.updateNextRunTime(null);
            String stackTrace = getStackTraceAsString(throwable);
            scheduledJobDetails.updateErrorLog(stackTrace);
        }
        scheduledJobDetails.updateCurrentlyRunningStatus(false);
    }

    private Scheduler getScheduler(final ScheduledJobDetail scheduledJobDetail) throws Exception {
        final String schedulername = getSchedulerName(scheduledJobDetail);
        Scheduler scheduler = this.schedulers.get(schedulername);
        if (scheduler == null) {
            int noOfThreads = SchedulerServiceConstants.DEFAULT_THREAD_COUNT;
            if (scheduledJobDetail.getSchedulerGroup() > 0) {
                noOfThreads = SchedulerServiceConstants.GROUP_THREAD_COUNT;
            }
            scheduler = createScheduler(schedulername, noOfThreads, schedulerJobListener);
            this.schedulers.put(schedulername, scheduler);
        }
        return scheduler;
    }

    @Override
    public void stopScheduler(String name) {
        Scheduler scheduler = this.schedulers.remove(name);
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getSchedulerName(final ScheduledJobDetail scheduledJobDetail) {
        StringBuilder sb = new StringBuilder(20);
        ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        sb.append(SchedulerServiceConstants.SCHEDULER).append(tenant.getTenantIdentifier());
        if (scheduledJobDetail.getSchedulerGroup() > 0) {
            sb.append(SchedulerServiceConstants.SCHEDULER_GROUP).append(scheduledJobDetail.getSchedulerGroup());
        }
        return sb.toString();
    }

    private Scheduler createScheduler(final String name, final int noOfThreads, JobListener... jobListeners) throws Exception {
        final SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(name);
        schedulerFactoryBean.setGlobalJobListeners(jobListeners);
        final TriggerListener[] globalTriggerListeners = { globalSchedulerTriggerListener };
        /*schedulerFactoryBean.setGlobalJobListeners(getGlobalListener(listenerClasses));
        TriggerListener globalTriggerListener = applicationContext.getBean("schedulerTriggerListener", TriggerListener.class);
        TriggerListener[] globalTriggerListeners = { globalTriggerListener };*/
        schedulerFactoryBean.setGlobalTriggerListeners(globalTriggerListeners);
        Properties quartzProperties = new Properties();
        quartzProperties.put(SchedulerFactoryBean.PROP_THREAD_COUNT, Integer.toString(noOfThreads));
        schedulerFactoryBean.setQuartzProperties(quartzProperties);
        schedulerFactoryBean.afterPropertiesSet();
        schedulerFactoryBean.start();
        return schedulerFactoryBean.getScheduler();
    }

    private JobDetail createJobDetail(final ScheduledJobDetail scheduledJobDetail) throws Exception {
    	
        ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        String[] jobDetails = CronMethodParser.findTargetMethodDetails(scheduledJobDetail.getJobName());
        Object targetObject = getBeanObject(Class.forName(jobDetails[CronMethodParser.CLASS_INDEX]));
        MethodInvokingJobDetailFactoryBean jobDetailFactoryBean = new MethodInvokingJobDetailFactoryBean();
        jobDetailFactoryBean.setName(scheduledJobDetail.getJobName() + "JobDetail" + tenant.getTenantIdentifier());
        jobDetailFactoryBean.setTargetObject(targetObject);
        jobDetailFactoryBean.setTargetMethod(jobDetails[CronMethodParser.METHOD_INDEX]);
        jobDetailFactoryBean.setGroup(scheduledJobDetail.getGroupName());
        jobDetailFactoryBean.setConcurrent(false);
        jobDetailFactoryBean.afterPropertiesSet();
        return jobDetailFactoryBean.getObject();
    }

    /**
     * @param jobDetails
     * @return
     * @throws ClassNotFoundException
     */
    private Object getBeanObject(Class<?> classType) throws ClassNotFoundException {
        List<Class<?>> typesList = new ArrayList<Class<?>>();
        Class<?>[] interfaceType = classType.getInterfaces();
        if (interfaceType.length > 0) {
            typesList.addAll(Arrays.asList(interfaceType));
        } else {
            Class<?> superclassType = classType;
            while (!Object.class.getName().equals(superclassType.getSuperclass().getName())) {
                superclassType = superclassType.getSuperclass();
            }
            typesList.add(superclassType);
        }
        List<String> beanNames = new ArrayList<String>();
        for (Class<?> clazz : typesList) {
            beanNames.addAll(Arrays.asList(this.applicationContext.getBeanNamesForType(clazz)));
        }
        Object targetObject = null;
        for (String beanName : beanNames) {
            Object nextObject = this.applicationContext.getBean(beanName);
            String targetObjName = nextObject.toString();
            targetObjName = targetObjName.substring(0, targetObjName.lastIndexOf("@"));
            if (classType.getName().equals(targetObjName)) {
                targetObject = nextObject;
                break;
            }
        }
        return targetObject;
    }

    /**
     * @param scheduledJobDetails
     * @return
     */
    private Trigger createTrigger(final ScheduledJobDetail scheduledJobDetails, final JobDetail jobDetail) {
    	
        ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
        CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
        cronTriggerFactoryBean.setName(scheduledJobDetails.getJobName() + "Trigger" + tenant.getTenantIdentifier());
        cronTriggerFactoryBean.setJobDetail(jobDetail);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(SchedulerServiceConstants.TENANT_IDENTIFIER, tenant.getTenantIdentifier());
        cronTriggerFactoryBean.setJobDataMap(jobDataMap);
        TimeZone timeZone = TimeZone.getTimeZone(tenant.getTimezoneId());
        cronTriggerFactoryBean.setTimeZone(timeZone);
        cronTriggerFactoryBean.setGroup(scheduledJobDetails.getGroupName());
        cronTriggerFactoryBean.setCronExpression(scheduledJobDetails.getCronExpression());
        cronTriggerFactoryBean.setPriority(scheduledJobDetails.getTaskPriority());
        cronTriggerFactoryBean.afterPropertiesSet();
        return cronTriggerFactoryBean.getObject();
    }

    /**
     * @param throwable
     * @return
     */
    private String getStackTraceAsString(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuffer sb = new StringBuffer(throwable.toString());
        for (StackTraceElement element : stackTraceElements) {
            sb.append("\n \t at ").append(element.getClassName()).append(".").append(element.getMethodName()).append("(")
                    .append(element.getLineNumber()).append(")");
        }
        return sb.toString();
    }

    private String getJobKeyAsString(final JobKey jobKey) {
        return jobKey.getName() + SchedulerServiceConstants.JOB_KEY_SEPERATOR + jobKey.getGroup();
    }

    private JobKey constructJobKey(final String Key) {
        String[] keyParams = Key.split(SchedulerServiceConstants.JOB_KEY_SEPERATOR);
        JobKey JobKey = new JobKey(keyParams[0], keyParams[1]);
        return JobKey;
    }

}