/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.accounting.autoposting.service;

import java.util.Date;
import java.util.Map;

import org.obsplatform.accounting.autoposting.api.AutoPostingJsonInputParams;
import org.obsplatform.accounting.autoposting.exception.AutoPostingDuplicateException;
import org.obsplatform.accounting.closure.api.GLClosureJsonInputParams;
import org.obsplatform.accounting.closure.command.GLClosureCommand;
import org.obsplatform.accounting.closure.domain.GLClosure;
import org.obsplatform.accounting.closure.domain.GLClosureRepository;
import org.obsplatform.accounting.closure.exception.GLClosureInvalidDeleteException;
import org.obsplatform.accounting.closure.exception.GLClosureInvalidException;
import org.obsplatform.accounting.closure.exception.GLClosureNotFoundException;
import org.obsplatform.accounting.closure.exception.GLClosureInvalidException.GL_CLOSURE_INVALID_REASON;
import org.obsplatform.accounting.closure.serialization.GLClosureCommandFromApiJsonDeserializer;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeRepository;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutoPostingWritePlatformServiceJpaRepositoryImpl implements AutoPostingWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(AutoPostingWritePlatformServiceJpaRepositoryImpl.class);

    private final GLClosureRepository glClosureRepository;
    private final OfficeRepository officeRepository;
    private final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public AutoPostingWritePlatformServiceJpaRepositoryImpl(final GLClosureRepository glClosureRepository,
            final OfficeRepository officeRepository, final GLClosureCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.glClosureRepository = glClosureRepository;
        this.officeRepository = officeRepository;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    @Transactional
    @Override
    public CommandProcessingResult createAutoPostingRule(final JsonCommand command) {
        try {
            final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
            closureCommand.validateForCreate();

            // check office is valid
            final Long officeId = command.longValueOfParameterNamed(GLClosureJsonInputParams.OFFICE_ID.getValue());
            final Office office = this.officeRepository.findOne(officeId);
            if (office == null) { throw new OfficeNotFoundException(officeId); }

            // TODO: Get Tenant specific date
            // ensure closure date is not in the future
            final Date todaysDate = DateUtils.getDateOfTenant();
            final Date closureDate = command.DateValueOfParameterNamed(GLClosureJsonInputParams.CLOSING_DATE.getValue());
            if (closureDate.after(todaysDate)) { throw new GLClosureInvalidException(GL_CLOSURE_INVALID_REASON.FUTURE_DATE, closureDate); }
            // shouldn't be before an existing accounting closure
            final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(officeId);
            if (latestGLClosure != null) {
                if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidException(
                        GL_CLOSURE_INVALID_REASON.ACCOUNTING_CLOSED, latestGLClosure.getClosingDate()); }
            }
            final GLClosure glClosure = GLClosure.fromJson(office, command);

            this.glClosureRepository.saveAndFlush(glClosure);

            return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(officeId)
                    .withEntityId(glClosure.getId()).build();
        } catch (final DataIntegrityViolationException dve) {
            handleAutoPostingIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateAutoPostingRule(final Long autoPostingId, final JsonCommand command) {
        final GLClosureCommand closureCommand = this.fromApiJsonDeserializer.commandFromApiJson(command.json());
        closureCommand.validateForUpdate();

        // is the glClosure valid
        final GLClosure glClosure = this.glClosureRepository.findOne(autoPostingId);
        if (glClosure == null) { throw new GLClosureNotFoundException(autoPostingId); }

        final Map<String, Object> changesOnly = glClosure.update(command);

        if (!changesOnly.isEmpty()) {
            this.glClosureRepository.saveAndFlush(glClosure);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withOfficeId(glClosure.getOffice().getId())
                .withEntityId(glClosure.getId()).with(changesOnly).build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteAutoPostingRule(final Long autoPostingId) {
        final GLClosure glClosure = this.glClosureRepository.findOne(autoPostingId);

        if (glClosure == null) { throw new GLClosureNotFoundException(autoPostingId); }

        /**
         * check if any closures are present for this branch at a later date
         * than this closure date
         **/
        final Date closureDate = glClosure.getClosingDate();
        final GLClosure latestGLClosure = this.glClosureRepository.getLatestGLClosureByBranch(glClosure.getOffice().getId());
        if (latestGLClosure.getClosingDate().after(closureDate)) { throw new GLClosureInvalidDeleteException(latestGLClosure.getOffice()
                .getId(), latestGLClosure.getOffice().getName(), latestGLClosure.getClosingDate()); }

        this.glClosureRepository.delete(glClosure);

        return new CommandProcessingResultBuilder().withOfficeId(glClosure.getOffice().getId()).withEntityId(glClosure.getId()).build();
    }

    /**
     * @param command
     * @param dve
     */
    private void handleAutoPostingIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {
        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("auto_posting_name_unique")) { throw new AutoPostingDuplicateException(
                command.stringValueOfParameterNamed(AutoPostingJsonInputParams.NAME.getValue())); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.glClosure.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource GL Closure: " + realCause.getMessage());
    }
}
