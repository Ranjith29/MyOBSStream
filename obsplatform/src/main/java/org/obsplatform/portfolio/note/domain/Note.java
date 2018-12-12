/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.note.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.group.domain.Group;
import org.obsplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_note")
public class Note extends AbstractAuditableCustom<AppUser,Long> {

	private static final long serialVersionUID = 1L;

	@ManyToOne
    @JoinColumn(name = "client_id", nullable = true)
    private final Client client;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @Column(name = "loan_id", nullable = true)
    private Long loan=null;

    @Column(name = "loan_transaction_id", nullable = true)
    private Long loanTransaction;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "note_type_enum")
    private final Integer noteTypeId;


    public static Note clientNoteFromJson(final Client client, final JsonCommand command) {
        final String note = command.stringValueOfParameterNamed("note");
        return new Note(client, note);
    }

    public static Note groupNoteFromJson(final Group group, final JsonCommand command) {
        final String note = command.stringValueOfParameterNamed("note");
        return new Note(group, note);
    }

    public static Note loanNote(final String note) {
        return new Note(note);
    }

    public static Note loanTransactionNote(final String note) {
        return new Note(note);
    }
    
    private Note(final Client client, final String note) {
        this.client = client;
        this.note = note;
        this.noteTypeId = NoteType.CLIENT.getValue();
    }

    private Note(final Group group, final String note) {
        this.group = group;
        this.note = note;
        this.client = null;
        this.noteTypeId = NoteType.GROUP.getValue();
    }

    private Note(String note) {
        
        this.client = null;
        this.note = note;
        this.noteTypeId = NoteType.LOAN_TRANSACTION.getValue();
    }

    protected Note() {
        this.client = null;
        this.group = null;
        this.loan = null;
        this.loanTransaction = null;
        this.note = null;
        this.noteTypeId = null;
    }


    public Map<String, Object> update(final JsonCommand command) {
        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String noteParamName = "note";
        if (command.isChangeInStringParameterNamed(noteParamName, this.note)) {
            final String newValue = command.stringValueOfParameterNamed(noteParamName);
            actualChanges.put(noteParamName, newValue);
            this.note = StringUtils.defaultIfEmpty(newValue, null);
        }
        return actualChanges;
    }

    public boolean isNotAgainstClientWithIdOf(final Long clientId) {
        return !this.client.identifiedBy(clientId);
    }

}