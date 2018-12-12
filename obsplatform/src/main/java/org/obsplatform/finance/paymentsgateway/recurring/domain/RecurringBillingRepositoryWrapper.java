/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.finance.paymentsgateway.recurring.domain;

import org.obsplatform.finance.paymentsgateway.recurring.exception.RecurringBillingNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link RecurringBillingRepository} .
 * </p>
 * 
 * @author ashokreddy
 */
@Service
public class RecurringBillingRepositoryWrapper {

	private final RecurringBillingRepository repository;

	@Autowired
	public RecurringBillingRepositoryWrapper(final RecurringBillingRepository repository) {
		this.repository = repository;
	}

	public void save(final RecurringBilling recurringBilling) {
		this.repository.save(recurringBilling);
	}
	
	public RecurringBilling findOneBySubscriberId(final String subscriberId) {
		return this.repository.findOneBySubscriberId(subscriberId);
	}
	
	public RecurringBilling findOneBySubscriberIdNotNull(final String subscriberId) {		
		final RecurringBilling recurringBilling = this.repository.findOneBySubscriberId(subscriberId);
		if(null == recurringBilling) {
			throw new RecurringBillingNotFoundException(subscriberId);
		}
		return recurringBilling;
	}
	
	public RecurringBilling findOneByOrderIdNotNull(final Long orderId) {		
		final RecurringBilling recurringBilling = this.repository.findOneByOrderId(orderId);
		if(null == recurringBilling) {
			throw new RecurringBillingNotFoundException(orderId);
		}
		return recurringBilling;
	}

	public RecurringBilling findOneByOrderId(final Long orderId) {
		return this.repository.findOneByOrderId(orderId);
	}

	public RecurringBilling findRecurringProfile(final Long clientId, final String subscriberId) {
		
		final RecurringBilling billing = this.repository.findOneByClientAndProfileId(subscriberId, clientId);
		if (null == billing) {
			throw new RecurringBillingNotFoundException(subscriberId, clientId);
		}
		return billing;
	}
	
	public RecurringBilling findRecurringProfile(final Long orderId) {
		
		final RecurringBilling recurringBilling = findOneByOrderId(orderId);	
		if (null == recurringBilling) {
			throw new RecurringBillingNotFoundException(orderId);
		}
		return recurringBilling;
	}

}
