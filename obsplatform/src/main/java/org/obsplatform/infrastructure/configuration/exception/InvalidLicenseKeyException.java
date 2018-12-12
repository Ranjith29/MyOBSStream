



package org.obsplatform.infrastructure.configuration.exception;

@SuppressWarnings("serial")
public class InvalidLicenseKeyException extends RuntimeException {

    public InvalidLicenseKeyException(final String message) {
    	 super(message);
    }
}