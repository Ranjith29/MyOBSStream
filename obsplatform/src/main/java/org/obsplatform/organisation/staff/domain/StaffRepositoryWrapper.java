/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.staff.domain;

import org.obsplatform.organisation.staff.exception.StaffNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link StaffRepository} that adds NULL checking and Error
 * handling capabilities
 * </p>
 */
@Service
public class StaffRepositoryWrapper {

    private final StaffRepository repository;

    @Autowired
    public StaffRepositoryWrapper(final StaffRepository repository) {
        this.repository = repository;
    }

    public Staff findOneWithNotFoundDetection(final Long id) {
        final Staff staff = this.repository.findOne(id);
        if (staff == null) { throw new StaffNotFoundException(id); }
        return staff;
    }

    public Staff findByOfficeWithNotFoundDetection(final Long staffId, final Long officeId) {
        final Staff staff = this.repository.findByOffice(staffId, officeId);
        if (staff == null) { throw new StaffNotFoundException(staffId); }
        return staff;
    }
}