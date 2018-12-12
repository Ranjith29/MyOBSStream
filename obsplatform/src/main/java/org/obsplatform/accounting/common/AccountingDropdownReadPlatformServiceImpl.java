/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.accounting.common;

import java.util.List;

import org.obsplatform.accounting.glaccount.domain.GLAccountType;
import org.obsplatform.accounting.glaccount.domain.GLAccountUsage;
import org.obsplatform.accounting.journalentry.domain.JournalEntryType;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.springframework.stereotype.Service;

@Service
public class AccountingDropdownReadPlatformServiceImpl implements AccountingDropdownReadPlatformService {

    @Override
    public List<EnumOptionData> retrieveGLAccountTypeOptions() {
        return AccountingEnumerations.gLAccountType(GLAccountType.values());
    }

    @Override
    public List<EnumOptionData> retrieveGLAccountUsageOptions() {
        return AccountingEnumerations.gLAccountUsage(GLAccountUsage.values());
    }

    @Override
    public List<EnumOptionData> retrieveJournalEntryTypeOptions() {
        return AccountingEnumerations.journalEntryTypes(JournalEntryType.values());
    }

}