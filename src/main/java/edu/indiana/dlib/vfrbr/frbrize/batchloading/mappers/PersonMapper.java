/**
 * Copyright 2009-2011, Trustees of Indiana University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of Indiana University nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers;

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.NormalIdent;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonDate;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;

/**
 *  Mapping a Person from a bibliographic MarcDataField
 * or an authority MarcRecord, and utility Person mapping methods.
 *
 */
public class PersonMapper {

    /**
     * Get a normalized authority-name based identifier string.
     * The identifier is a "normalized" concatenation of subfields "aqbcd"
     * from the parameter MarcDataField.  A person has one
     * authIdent based on the authority-name mapping.  The authIdent
     * is the primary content-based identification for the person.
     *
     * @param personField source person MarcDataField.
     * @return String of normalized authority name based identifier.
     */
    public static String getAuthIdent(final MarcDataField personField) {

        return NormalIdent.getAuthIdent(
                personField.concatSubfields("aqbcd".toCharArray()));
    }

    /**
     * Get a normalized name-date based identifier string.
     * The normalName is constructed by normalizing a combination
     * of a person name and date.  There is a normalName
     * for each name of a person.  The normalName is used
     * for a "normalized" matching with persisted person names,
     * as a backup to the authIdent identification.
     *
     * @param personField the source person MarcDataField.
     * @return the String value of normalized name-date identifier.
     */
    public static String getNormalName(final MarcDataField personField) {

        return NormalIdent.getNormalIdent(
                getPersistNameString(personField)
                + "_"
                + getPersistDateString(personField));
    }

    /**
     * Utility method to provide the person name
     * from a person MarcDataField as that name
     * would be persisted according to the mapping specification.
     *
     * @param personField MarcDataField for person.
     * @return String name.
     */
    public static String getPersistNameString(
            final MarcDataField personField) {

        return personField.concatSubfields("aq".toCharArray());
    }

    /**
     * Utility method to provide the person date
     * from a person MarcDataField as that date
     * would be persisted according to the mapping specification.
     *
     * @param personField MarcDataField for person.
     * @return String for date.
     */
    protected static String getPersistDateString(
            final MarcDataField personField) {

        String dateValue = null;

        // -- datesOfPerson |d (NR)
        final String dateString = personField.getValue('d');

        if (null != dateString) {
            final PersonDate persDate = new PersonDate();
            DateNormalizer.normalizePersonDate(dateString, persDate);
            dateValue = persDate.getText();
        }

        return dateValue;
    }

    /**
     * Map Person from authority MarcRecord.
     * The parameter person is updated with values
     * from the parameter authority MarcRecord
     * in accordance with the mapping specification.
     * .
     * @param authRec source authority MarcRecord.
     * @param person PersonJpa person to be updated.
     */
    public final void mapFromAuthRecord(final MarcRecord authRec,
                                        final PersonJpa person) {

        new PersAuthMap().mapAuthRec(authRec, person);
    }

    /**
     * Map a Person from a MARC bibliographic field.
     * @param personField the MarcDataField holding the MARC field
     * @param person PersonJpa person to be mapped
     */
    public final void mapFromBibField(final MarcDataField personField,
                                      final PersonJpa person) {

        new PersBibMap().mapBibField(personField, person);
    }
}
