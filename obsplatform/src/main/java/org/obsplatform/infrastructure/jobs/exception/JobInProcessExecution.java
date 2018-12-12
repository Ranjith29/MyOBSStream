package org.obsplatform.infrastructure.jobs.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;


public class JobInProcessExecution extends AbstractPlatformResourceNotFoundException {

    public JobInProcessExecution(String identifier) {
        super("error.msg.sheduler.job.inprogress", "job execution is in process for "+identifier, identifier);
    }

}
