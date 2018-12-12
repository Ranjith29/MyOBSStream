/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.vendoragreement.service;

import java.io.IOException;

import org.json.JSONException;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.vendoragreement.data.VendorAgreementData;

public interface VendorAgreementWritePlatformService {

    CommandProcessingResult createVendorAgreement(JsonCommand command);

	CommandProcessingResult updateVendorAgreement(Long vendorAgreementId, JsonCommand command);

}
