/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.service;

import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.obsplatform.infrastructure.security.exception.NoAuthorizationException;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.monetary.domain.ApplicationCurrency;
import org.obsplatform.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.obsplatform.organisation.monetary.domain.MonetaryCurrency;
import org.obsplatform.organisation.monetary.domain.Money;
import org.obsplatform.organisation.office.domain.Office;
import org.obsplatform.organisation.office.domain.OfficeAddress;
import org.obsplatform.organisation.office.domain.OfficeAddressRepository;
import org.obsplatform.organisation.office.domain.OfficeRepository;
import org.obsplatform.organisation.office.domain.OfficeTransaction;
import org.obsplatform.organisation.office.domain.OfficeTransactionRepository;
import org.obsplatform.organisation.office.exception.OfficeNotFoundException;
import org.obsplatform.organisation.office.serialization.OfficeCommandFromApiJsonDeserializer;
import org.obsplatform.organisation.office.serialization.OfficeTransactionCommandFromApiJsonDeserializer;
import org.obsplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfficeWritePlatformServiceJpaRepositoryImpl implements OfficeWritePlatformService {

    private final static Logger LOGGER = LoggerFactory.getLogger(OfficeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    private final OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer;
    private final OfficeRepository officeRepository;
    private final OfficeTransactionRepository officeTransactionRepository;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;
    private final OfficeAddressRepository addressRepository;

    @Autowired
    public OfficeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context,
            final OfficeCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            final OfficeTransactionCommandFromApiJsonDeserializer moneyTransferCommandFromApiJsonDeserializer,
            final OfficeRepository officeRepository, final OfficeTransactionRepository officeMonetaryTransferRepository,
            final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository,final OfficeAddressRepository addressRepository) {
    	
        this.context = context;
        this.fromApiJsonDeserializer = fromApiJsonDeserializer;
        this.moneyTransferCommandFromApiJsonDeserializer = moneyTransferCommandFromApiJsonDeserializer;
        this.officeRepository = officeRepository;
        this.officeTransactionRepository = officeMonetaryTransferRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    @Override
    public CommandProcessingResult createOffice(final JsonCommand command) {

        try {
            final AppUser currentUser = context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForCreate(command.json());

            Long parentId = null;
            if (command.parameterExists("parentId")) {
                parentId = command.longValueOfParameterNamed("parentId");
            }

            final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, parentId);
            final Office office = Office.fromJson(parent, command);
            
            OfficeAddress address =OfficeAddress.fromJson(command,office);
            office.setOfficeAddress(address);

            // pre save to generate id for use in office hierarchy
            this.officeRepository.save(office);

            office.generateHierarchy();

            this.officeRepository.save(office);

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(office.getId()) //
                    .withOfficeId(office.getId()) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult updateOffice(final Long officeId, final JsonCommand command) {

        try {
            final AppUser currentUser = context.authenticatedUser();

            this.fromApiJsonDeserializer.validateForUpdate(command.json());

            Long parentId = null;
            if (command.parameterExists("parentId")) {
                parentId = command.longValueOfParameterNamed("parentId");
            }

            final Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, officeId);

            final Map<String, Object> changes = office.update(command);

            if (changes.containsKey("parentId")) {
                final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, parentId);
                office.update(parent);
            }
            
            //update officeAddress
            final  OfficeAddress officeAddress  = this.addressRepository.findOneWithPartnerId(office);
            final Map<String, Object> addressChanges = officeAddress.update(command);
            if(!addressChanges.isEmpty()){
            	office.setOfficeAddress(officeAddress);
 		    }

            if (!changes.isEmpty()) {
                this.officeRepository.saveAndFlush(office);
            }

            return new CommandProcessingResultBuilder() //
                    .withCommandId(command.commandId()) //
                    .withEntityId(office.getId()) //
                    .withOfficeId(office.getId()) //
                    .with(changes) //
                    .build();
        } catch (DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return CommandProcessingResult.empty();
        }
    }

    @Transactional
    @Override
    public CommandProcessingResult officeTransaction(final JsonCommand command) {

        context.authenticatedUser();

        this.moneyTransferCommandFromApiJsonDeserializer.validateOfficeTransfer(command.json());

        Long officeId = null;
        Office fromOffice = null;
        final Long fromOfficeId = command.longValueOfParameterNamed("fromOfficeId");
        if (fromOfficeId != null) {
            fromOffice = this.officeRepository.findOne(fromOfficeId);
            officeId = fromOffice.getId();
        }
        Office toOffice = null;
        final Long toOfficeId = command.longValueOfParameterNamed("toOfficeId");
        if (toOfficeId != null) {
            toOffice = this.officeRepository.findOne(toOfficeId);
            officeId = toOffice.getId();
        }

        if (fromOffice == null && toOffice == null) { throw new OfficeNotFoundException(toOfficeId); }

        final String currencyCode = command.stringValueOfParameterNamed("currencyCode");
        final ApplicationCurrency appCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currencyCode);

        final MonetaryCurrency currency = new MonetaryCurrency(appCurrency.getCode(), appCurrency.getDecimalPlaces());
        final Money amount = Money.of(currency, command.bigDecimalValueOfParameterNamed("transactionAmount"));

        final OfficeTransaction entity = OfficeTransaction.fromJson(fromOffice, toOffice, amount, command);

        this.officeTransactionRepository.save(entity);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(entity.getId()) //
                .withOfficeId(officeId) //
                .build();
    }

    @Transactional
    @Override
    public CommandProcessingResult deleteOfficeTransaction(final Long transactionId, final JsonCommand command) {

        context.authenticatedUser();

        this.officeTransactionRepository.delete(transactionId);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(transactionId) //
                .build();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleOfficeDataIntegrityIssues(final JsonCommand command, final DataIntegrityViolationException dve) {

        final Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("externalid_org")) {
            final String externalId = command.stringValueOfParameterNamed("externalId");
            throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId `" + externalId
                    + "` already exists", "externalId", externalId);
        } else if (realCause.getMessage().contains("name_org")) {
            final String name = command.stringValueOfParameterNamed("name");
            throw new PlatformDataIntegrityException("error.msg.office.duplicate.name", "Office with name `" + name + "` already exists",
                    "name", name);
        }

        LOGGER.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    /*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    public Office validateUserPriviledgeOnOfficeAndRetrieve(final AppUser currentUser, final Long officeId) {

        final Long userOfficeId = currentUser.getOffice().getId();
        final Office userOffice = this.officeRepository.findOne(userOfficeId);
        if (userOffice == null) { throw new OfficeNotFoundException(userOfficeId); }

        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) { throw new NoAuthorizationException(
                "User does not have sufficient priviledges to act on the provided office."); }

        Office officeToReturn = userOffice;
        if (!userOffice.identifiedBy(officeId)) {
            officeToReturn = this.officeRepository.findOne(officeId);
            if (officeToReturn == null) { throw new OfficeNotFoundException(officeId); }
        }

        return officeToReturn;
    }
    
}