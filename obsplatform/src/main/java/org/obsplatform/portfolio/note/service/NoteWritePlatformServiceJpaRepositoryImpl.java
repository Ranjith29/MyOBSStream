/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.note.service;

import java.util.Map;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepositoryWrapper;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.group.domain.Group;
import org.obsplatform.portfolio.group.domain.GroupRepository;
import org.obsplatform.portfolio.group.exception.GroupNotFoundException;
import org.obsplatform.portfolio.note.domain.Note;
import org.obsplatform.portfolio.note.domain.NoteRepository;
import org.obsplatform.portfolio.note.domain.NoteType;
import org.obsplatform.portfolio.note.exception.NoteNotFoundException;
import org.obsplatform.portfolio.note.exception.NoteResourceNotSupportedException;
import org.obsplatform.portfolio.note.serialization.NoteCommandFromApiJsonDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NoteWritePlatformServiceJpaRepositoryImpl implements NoteWritePlatformService {

    private final NoteRepository noteRepository;
    private final ClientRepositoryWrapper clientRepository;
    private final GroupRepository groupRepository;
    // private final SavingAccountRepository savingAccountRepository;
   
    private final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer;

    @Autowired
    public NoteWritePlatformServiceJpaRepositoryImpl(final NoteRepository noteRepository, final ClientRepositoryWrapper clientRepository,
            final GroupRepository groupRepository, 
            final NoteCommandFromApiJsonDeserializer fromApiJsonDeserializer) {
        this.noteRepository = noteRepository;
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
         this.fromApiJsonDeserializer = fromApiJsonDeserializer;
    }

    private CommandProcessingResult createClientNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);
        if (client == null) { throw new ClientNotFoundException(resourceId); }
        final Note newNote = Note.clientNoteFromJson(client, command);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withClientId(client.getId()) //
                .withOfficeId(client.officeId()) //
                .build();

    }

    private CommandProcessingResult createGroupNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();

        final Group group = this.groupRepository.findOne(resourceId);
        if (group == null) { throw new GroupNotFoundException(resourceId); }
        final Note newNote = Note.groupNoteFromJson(group, command);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withClientId(group.getId()) //
                .withOfficeId(group.officeId()) //
                .build();
    }

  /*  private CommandProcessingResult createLoanNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();

       // final Loan loan = this.loanRepository.findOne(resourceId);
       // if (loan == null) { throw new LoanNotFoundException(resourceId); }

        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanNote( note);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withClientId(1l) //
                .withOfficeId(1l) //
                .withLoanId(1l) //
                .withGroupId(1l) //
                .build();
    }*/

    private CommandProcessingResult createLoanTransactionNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();

     //   final LoanTransaction loanTransaction = this.loanTransactionRepository.findOne(resourceId);
      //  if (loanTransaction == null) { throw new LoanTransactionNotFoundException(resourceId); }

    //    final Loan loan = loanTransaction.getLoan();

        final String note = command.stringValueOfParameterNamed("note");
        final Note newNote = Note.loanTransactionNote(note);

        this.noteRepository.save(newNote);

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(newNote.getId()) //
                .withClientId(1l).withOfficeId(1l).withLoanId(1l).withGroupId(1l)// Loan
                                                                                                                                          // can
                                                                                                                                          // be
                                                                                                                                          // associated
                .build();
    }

    // private CommandProcessingResult createSavingAccountNote(final JsonCommand
    // command) {
    //
    // final Long resourceId = command.getSupportedEntityId();
    //
    // final SavingAccount savingAccount =
    // this.savingAccountRepository.findOne(resourceId);
    // if (savingAccount == null) { throw new
    // SavingAccountNotFoundException(resourceId); }
    //
    // final String note = command.stringValueOfParameterNamed("note");
    // final Note newNote = Note.savingNote(savingAccount, note);
    //
    // this.noteRepository.save(newNote);
    //
    // return new CommandProcessingResultBuilder() //
    // .withCommandId(command.commandId()) //
    // .withEntityId(newNote.getId()) //
    // .withClientId(savingAccount.getClient().getId()).withOfficeId(savingAccount.getClient().getOffice().getId()).build();
    // }

    @Override
    public CommandProcessingResult createNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final String resourceUrl = command.getSupportedEntityType();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        switch (type) {
            case CLIENT: {
                return createClientNote(command);
            }
            case GROUP: {
                return createGroupNote(command);
            }
           
            
            // case SAVING_ACCOUNT: {
            // return createSavingAccountNote(command);
            // }
            default:
                throw new NoteResourceNotSupportedException(resourceUrl);
        }

    }

    private CommandProcessingResult updateClientNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final String resourceUrl = command.getSupportedEntityType();

        final NoteType type = NoteType.fromApiUrl(resourceUrl);

        final Client client = this.clientRepository.findOneWithNotFoundDetection(resourceId);

       final Note noteForUpdate = this.noteRepository.findByClientIdAndId(resourceId, noteId);
        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(noteForUpdate.getId()) //
                .withClientId(client.getId()) //
                .withOfficeId(client.officeId()) //
                .with(changes) //
                .build();
    }

    private CommandProcessingResult updateGroupNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final String resourceUrl = command.getSupportedEntityType();

        final NoteType type = NoteType.fromApiUrl(resourceUrl);

        final Group group = this.groupRepository.findOne(resourceId);
        if (group == null) { throw new GroupNotFoundException(resourceId); }
        final Note noteForUpdate = this.noteRepository.findByGroupIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder() //
                .withCommandId(command.commandId()) //
                .withEntityId(noteForUpdate.getId()) //
                .withGroupId(group.getId()) //
                .withOfficeId(group.officeId()) //
                .with(changes).build();
    }

   /* private CommandProcessingResult updateLoanNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final String resourceUrl = command.getSupportedEntityType();

        final NoteType type = NoteType.fromApiUrl(resourceUrl);

       // final Loan loan = this.loanRepository.findOne(resourceId);
    //    if (loan == null) { throw new LoanNotFoundException(resourceId); }
        final Note noteForUpdate = this.noteRepository.findByLoanIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(noteForUpdate.getId())
                .withClientId(1l).withLoanId(1l).withGroupId(1l).withOfficeId(1l)
                .with(changes).build();
    }*/

  /*  private CommandProcessingResult updateLoanTransactionNote(final JsonCommand command) {

        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final String resourceUrl = command.getSupportedEntityType();

        final NoteType type = NoteType.fromApiUrl(resourceUrl);

     //   final LoanTransaction loanTransaction = this.loanTransactionRepository.findOne(resourceId);
     //   if (loanTransaction == null) { throw new LoanTransactionNotFoundException(resourceId); }
     //   final Loan loan = loanTransaction.getLoan();

        final Note noteForUpdate = this.noteRepository.findByLoanTransactionIdAndId(resourceId, noteId);

        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }

        final Map<String, Object> changes = noteForUpdate.update(command);

        if (!changes.isEmpty()) {
            this.noteRepository.saveAndFlush(noteForUpdate);
        }

        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(noteForUpdate.getId())
                .withClientId(1l).withLoanId(1l).withGroupId(1l).withOfficeId(1l)
                .with(changes).build();
    }*/

    // private CommandProcessingResult updateSavingAccountNote(final JsonCommand
    // command) {
    //
    // final Long resourceId = command.getSupportedEntityId();
    // final Long noteId = command.entityId();
    // final String resourceUrl = command.getSupportedEntityType();
    //
    // final NoteType type = NoteType.fromApiUrl(resourceUrl);
    //
    // final SavingAccount savingAccount =
    // this.savingAccountRepository.findOne(resourceId);
    // if (savingAccount == null) { throw new
    // SavingAccountNotFoundException(resourceId); }
    //
    // final Note noteForUpdate =
    // this.noteRepository.findBySavingAccountIdAndId(resourceId, noteId);
    //
    // if (noteForUpdate == null) { throw new NoteNotFoundException(noteId,
    // resourceId, type.name().toLowerCase()); }
    //
    // final Map<String, Object> changes = noteForUpdate.update(command);
    //
    // if (!changes.isEmpty()) {
    // this.noteRepository.saveAndFlush(noteForUpdate);
    // }
    //
    // return new CommandProcessingResultBuilder()
    // //
    // .withCommandId(command.commandId())
    // //
    // .withEntityId(noteForUpdate.getId())
    // //
    // .withClientId(savingAccount.getClient().getId()).withOfficeId(savingAccount.getClient().getOffice().getId()).with(changes)
    // .build();
    // }

    @Override
    public CommandProcessingResult updateNote(final JsonCommand command) {

        this.fromApiJsonDeserializer.validateNote(command.json());

        final String resourceUrl = command.getSupportedEntityType();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);

        switch (type) {
            case CLIENT: {
                return updateClientNote(command);
            }
            case GROUP: {
                return updateGroupNote(command);
            }
           /* case LOAN: {
                return updateLoanNote(command);
            }
            case LOAN_TRANSACTION: {
                return updateLoanTransactionNote(command);
            }*/
            // case SAVING_ACCOUNT: {
            // return updateSavingAccountNote(command);
            // }
            default:
                throw new NoteResourceNotSupportedException(resourceUrl);
        }
    }

    @Override
    public CommandProcessingResult deleteNote(final JsonCommand command) {

        final Note noteForDelete = getNoteForDelete(command);

        this.noteRepository.delete(noteForDelete);
        return new CommandProcessingResultBuilder() //
                .withCommandId(null) //
                .withEntityId(command.entityId()) //
                .build();
    }

    private Note getNoteForDelete(final JsonCommand command) {
        final String resourceUrl = command.getSupportedEntityType();
        final Long resourceId = command.getSupportedEntityId();
        final Long noteId = command.entityId();
        final NoteType type = NoteType.fromApiUrl(resourceUrl);
        Note noteForUpdate = null;
        switch (type) {
            case CLIENT: {
                noteForUpdate = this.noteRepository.findByClientIdAndId(resourceId, noteId);
            }
            break;
            case GROUP: {
                noteForUpdate = this.noteRepository.findByGroupIdAndId(resourceId, noteId);
            }
            break;
           /* case LOAN: {
                noteForUpdate = this.noteRepository.findByLoanIdAndId(resourceId, noteId);
            }
            break;
            case LOAN_TRANSACTION: {
                noteForUpdate = this.noteRepository.findByLoanTransactionIdAndId(resourceId, noteId);
            }
            break;*/
            // case SAVING_ACCOUNT: {
            // noteForUpdate =
            // this.noteRepository.findBySavingAccountIdAndId(resourceId,
            // noteId);
            // }
            // break;
            case SAVING_ACCOUNT:
            break;
        }
        if (noteForUpdate == null) { throw new NoteNotFoundException(noteId, resourceId, type.name().toLowerCase()); }
        return noteForUpdate;
    }

}
