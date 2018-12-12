package org.obsplatform.billing.loginhistory.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when a code is not found.
 */
public class LoginHistoryNotFoundException extends AbstractPlatformResourceNotFoundException {

    public LoginHistoryNotFoundException(final String loginHistoryId) {
        super("error.msg.loginhistory.not.found", "LoginHistory with this id"+loginHistoryId+"not exist",loginHistoryId);
        
    }

   
}
