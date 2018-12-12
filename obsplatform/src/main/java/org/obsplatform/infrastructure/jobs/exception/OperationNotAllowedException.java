package org.obsplatform.infrastructure.jobs.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformServiceUnavailableException;


/**
 * A {@link RuntimeException} thrown when Job execution is in progress.
 */
public class OperationNotAllowedException extends AbstractPlatformServiceUnavailableException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OperationNotAllowedException(final String jobNames) {
        super("error.msg.sheduler.job.currently.running", "Execution is in-process for jobs " + jobNames
                + "...., so update operations are not allowed at this moment", jobNames);
    }
}