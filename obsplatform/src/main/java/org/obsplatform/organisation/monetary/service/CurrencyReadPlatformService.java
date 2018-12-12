/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.monetary.service;

import java.util.Collection;

import org.obsplatform.organisation.monetary.data.CurrencyData;

public interface CurrencyReadPlatformService {

	Collection<CurrencyData> retrieveAllowedCurrencies();

	Collection<CurrencyData> retrieveAllPlatformCurrencies();
}