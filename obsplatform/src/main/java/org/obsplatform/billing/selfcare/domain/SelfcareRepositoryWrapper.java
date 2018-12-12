/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.billing.selfcare.domain;

import org.obsplatform.billing.selfcare.exception.SelfCareNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link SelfcareRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class SelfcareRepositoryWrapper {

	private final SelfCareRepository repository;

	@Autowired
	public SelfcareRepositoryWrapper(final SelfCareRepository repository) {
		this.repository = repository;
	}

	public SelfCare findOneWithNotFoundDetection(final Long id) {
		final SelfCare selfCare = this.repository.findOne(id);
		if (selfCare == null) {
			throw new SelfCareNotFoundException(id);
		}
		return selfCare;
	}

	public void save(final SelfCare selfCare) {
		this.repository.save(selfCare);
	}

	public void saveAndFlush(final SelfCare selfCare) {
		this.repository.saveAndFlush(selfCare);
	}

	public void delete(final SelfCare selfCare) {
		this.repository.delete(selfCare);
	}
	
	public SelfCare findOneByClientId(final Long clientId) {
		try {
			return this.repository.findOneByClientId(clientId);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
			return null;
		}
	}

}
