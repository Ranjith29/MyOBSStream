/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.core.service;

public class PlatformEmailSendException extends RuntimeException {

    public PlatformEmailSendException(final Throwable e) {
        super(e);
    }
}
