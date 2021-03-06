/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.exception;

/**
 * A {@link RuntimeException} thrown when resources that are queried for are not
 * found.
 */
public abstract class AbstractPlatformServiceUnavailableException extends RuntimeException {

    private final String globalisationMessageCode;
    private final String defaultUserMessage;
    private final Object[] defaultUserMessageArgs;

    public AbstractPlatformServiceUnavailableException(final String globalisationMessageCode, final String defaultUserMessage,
            final Object... defaultUserMessageArgs) {
        this.globalisationMessageCode = globalisationMessageCode;
        this.defaultUserMessage = defaultUserMessage;
        this.defaultUserMessageArgs = defaultUserMessageArgs;
    }

    public String getGlobalisationMessageCode() {
        return globalisationMessageCode;
    }

    public String getDefaultUserMessage() {
        return defaultUserMessage;
    }

    public Object[] getDefaultUserMessageArgs() {
        return defaultUserMessageArgs;
    }
}