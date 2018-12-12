/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.accounting.glaccount.service;

import java.util.List;

import org.obsplatform.accounting.glaccount.data.GLAccountData;
import org.obsplatform.accounting.glaccount.domain.GLAccountType;

public interface GLAccountReadPlatformService {

    List<GLAccountData> retrieveAllGLAccounts(Integer accountClassification, String searchParam, Integer usage,
            Boolean manualTransactionsAllowed, Boolean disabled);

    GLAccountData retrieveGLAccountById(long glAccountId);

    List<GLAccountData> retrieveAllEnabledDetailGLAccounts();

    List<GLAccountData> retrieveAllEnabledDetailGLAccounts(GLAccountType accountType);

    GLAccountData retrieveNewGLAccountDetails();
}
