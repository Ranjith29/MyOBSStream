package org.obsplatform.infrastructure.jobs.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;

public class JobParameterNotConfiguredException extends AbstractPlatformServiceUnavailableException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JobParameterNotConfiguredException(final String jobName) {
        super("error.msg.sheduler.job.parameter.not.configure", "Job Parameters for the " + jobName
                + " job is not configured Properly", jobName);
    }
}
