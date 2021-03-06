/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.portfolio.note.command;

/**
 * Immutable command used for create or update of notes.
 */
public class NoteCommand {

    @SuppressWarnings("unused")
    private final String note;

    public NoteCommand(final String note) {
        this.note = note;
    }

}