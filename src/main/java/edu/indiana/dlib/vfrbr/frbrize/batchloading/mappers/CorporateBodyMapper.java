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
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;

/**
 * Implementation of mappings from authority and bibliographic MARC records
 * and CorporateBody entity, as well as other public utility methods.
 */
public class CorporateBodyMapper {

    /**
     * Get a normalized authority-name based identifier string.
     * The identifier is a "normalized" concatenation of subfields "abcde"
     * from the parameter MarcDataField.  A corporateBody has one
     * authIdent based on the authority-name mapping.  The authIdent
     * is the primary content-based identification for the corporateBody.
     *
     * @param corpBodyField source corporateBody MarcDataField.
     * @return String of normalized authority name based identifier.
     */
    public static String getAuthIdent(final MarcDataField corpBodyField) {

        return NormalIdent.getAuthIdent(
                corpBodyField.concatSubfields("abcde".toCharArray()));
    }

    /**
     * Get a normalized name-date based identifier string.
     * The normalName is constructed by normalizing a combination
     * of a corporateBody name and date.  There is a normalName
     * for each name of a corporateBody.  The normalName is used
     * for a "normalized" matching with persisted corporateBody names,
     * as a backup to the authIdent identification.
     *
     * @param corporateField the source corporateBody MarcDataField.
     * @return the String value of normalized name-date identifier.
     */
    public static String getNormalName(final MarcDataField corporateField) {

        return NormalIdent.getNormalIdent(
                getPersistNameString(corporateField)
                + "_"
                + getPersistDateString(corporateField));
    }

    /**
     * Utility method to provide the corporateBody name
     * from a corporateBody MarcDataField as that name
     * would be persisted according to the mapping specification.
     *
     * @param corpBodyField MarcDataField for corporateBody.
     * @return String name.
     */
    protected static String getPersistNameString(
            final MarcDataField corpBodyField) {
        // persisting spec uses |a (NR) |b (R) |e (R)

        return corpBodyField.concatSubfields("abe".toCharArray());
    }

    /**
     * Utility method to provide the corporateBody date
     * from a corporateBody MarcDataField as that date
     * would be persisted according to the mapping specification.
     *
     * @param corpBodyField MarcDataField for CorporateBody.
     * @return String for date.
     */
    protected static String getPersistDateString(
            final MarcDataField corpBodyField) {
        // |d (R)
        // TODO this returns only the first found of repeating subfield.

        return corpBodyField.getValue('d');
    }

    /**
     * Map CorporateBody from authority MarcRecord.
     * The parameter corporateBody is updated with values
     * from the parameter authority MarcRecord
     * in accordance with the mapping specification.
     * .
     * @param authRec source authority MarcRecord.
     * @param corpBody CorporateBodyJpa corporateBody to be updated.
     */
    public final void mapFromAuthRecord(final MarcRecord authRec,
                                        final CorporateBodyJpa corpBody) {

        new CorpAuthMap().mapAuthRec(authRec, corpBody);
    }

    /**
     * Map from a bibliographic MarcDataFieldto a CorporateBodyJpa.
     *
     * @param bibCorpField a 110, 111, 610, 611, 710, or 711
     * MarcDataField for a CorporateBody.
     * @param corpBody the CorporateBodyJpa instance to be mapped.
     */
    public final void mapFromBibField(final MarcDataField bibCorpField,
                                      final CorporateBodyJpa corpBody) {

        new CorpBibMap().mapBibField(bibCorpField, corpBody);
    }
}
