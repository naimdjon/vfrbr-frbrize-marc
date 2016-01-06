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
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyDate;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyHistory;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyName;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyNote;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyNumber;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyPlace;
import java.util.List;

/**
 *  Mapping of a CorporateBody from an authority MarcRecord.
 *
 */
final class CorpAuthMap {

    /**
     * Map CorporateBody from authority MarcRecord.
     * The parameter corporateBody is updated with values
     * from the parameter authority MarcRecord
     * in accordance with the mapping specification.
     * .
     * @param authRec source authority MarcRecord.
     * @param corpBody CorporateBodyJpa corporateBody to be updated.
     */
    protected void mapAuthRec(final MarcRecord authRec,
                              final CorporateBodyJpa corpBody) {
        /*
         * -- nameOfTheCorporateBody
         */
        // 110,111 (NR) |a (NR) |b (R) |e (R)
        // authorized name
        final String[] headingFields = {"110", "111"};
        final List<MarcDataField> headingFieldList = authRec.getDataFields(
                headingFields);

        for (MarcDataField headingField : headingFieldList) {
            if (headingField.hasSubfields("abe".toCharArray())) {
                final CorporateBodyName authName =
                        new CorporateBodyName(corpBody);
                authName.setText(headingField.concatSubfields(
                        "abe".toCharArray()));
                authName.setType("authorized");
                authName.setVocabulary("naf");
                authName.setNormal(CorporateBodyMapper.getNormalName(
                        headingField));

                corpBody.getNames().add(authName);
                corpBody.setAuthorizedName(authName.getText());
            }
        }

        // -- authIdent
        // from first element of headingFieldList above
        if (!headingFieldList.isEmpty()) {
            corpBody.setAuthIdent(
                    CorporateBodyMapper.getAuthIdent(headingFieldList.get(0)));
        }


        // 410,411 (R) |a (NR) |b (R) |e (R)
        // variant name
        final String[] seeFromFields = {"410", "411"};
        final List<MarcDataField> seeFromFieldList =
                authRec.getDataFields(seeFromFields);
        for (MarcDataField seeFromField : seeFromFieldList) {
            if (seeFromField.hasSubfields("abe".toCharArray())) {
                final CorporateBodyName varName =
                        new CorporateBodyName(corpBody);
                varName.setText(
                        seeFromField.concatSubfields("abe".toCharArray()));
                varName.setType("variant");
                varName.setNormal(
                        CorporateBodyMapper.getNormalName(seeFromField));

                corpBody.getNames().add(varName);
            }
        }

        /*
         * -- numberAssociatedWithTheCorporatebody
         */
        // |n (R)
        boolean foundHeadingNumber = false;
        for (MarcDataField headingField : headingFieldList) {
            if (!headingField.hasSubfields("kt".toCharArray())) {
                for (String numVal : headingField.getValueList('n')) {

                    final CorporateBodyNumber corpNum =
                            new CorporateBodyNumber(corpBody);
                    corpNum.setText(numVal);

                    corpBody.getNumbers().add(corpNum);
                    foundHeadingNumber = true;
                }
            }
        }
        if (!foundHeadingNumber) {
            for (MarcDataField seeFromField : seeFromFieldList) {
                if (!seeFromField.hasSubfields("kt".toCharArray())) {
                    for (String numVal : seeFromField.getValueList('n')) {

                        final CorporateBodyNumber corpNum =
                                new CorporateBodyNumber(corpBody);
                        corpNum.setText(numVal);

                        corpBody.getNumbers().add(corpNum);
                    }
                }
            }
        }


        /*
         * -- placeAssociatedWithTheCorporateBody
         */
        // |c (NR)
        boolean foundHeadingPlace = false;
        for (MarcDataField headingField : headingFieldList) {
            if (headingField.hasSubfields("c".toCharArray())) {

                final CorporateBodyPlace corpPlace =
                        new CorporateBodyPlace(corpBody);
                corpPlace.setText(headingField.getValue('c'));

                corpBody.getPlaces().add(corpPlace);
                foundHeadingPlace = true;
            }
        }
        if (!foundHeadingPlace) {
            for (MarcDataField seeFromField : seeFromFieldList) {
                if (seeFromField.hasSubfields("c".toCharArray())) {

                    final CorporateBodyPlace corpPlace =
                            new CorporateBodyPlace(corpBody);
                    corpPlace.setText(seeFromField.getValue('c'));

                    corpBody.getPlaces().add(corpPlace);
                }
            }
        }

        /*
         * -- dateAssociatedWithTheCorporateBody
         */
        // |d (R)
        boolean foundHeadingDate = false;
        for (MarcDataField headingField : headingFieldList) {
            for (String dateField : headingField.getValueList('d')) {

                final CorporateBodyDate corpDate =
                        new CorporateBodyDate(corpBody);
                DateNormalizer.normalizeCorporateDate(dateField, corpDate);

                corpBody.getDates().add(corpDate);
                foundHeadingDate = true;
            }
        }
        if (!foundHeadingDate) {
            for (MarcDataField seeFromField : seeFromFieldList) {
                for (String dateField : seeFromField.getValueList('d')) {

                    final CorporateBodyDate corpDate =
                            new CorporateBodyDate(corpBody);
                    DateNormalizer.normalizeCorporateDate(dateField, corpDate);

                    corpBody.getDates().add(corpDate);
                }
            }
        }

        /*
         * -- otherDesignationAssociatedWithTheCorporateBody
         */
        // not mapped from authority

        /*
         * -- languageOfTheCorporateBody
         */
        // not mapped from authority

        /*
         * -- address
         */
        // not mapped from authority

        /*
         * -- fieldOfActivity
         */
        // not mapped from authority

        /*
         * -- history
         */
        // 678 (R)
        for (MarcDataField field678 : authRec.getDataFields("678")) {

            final CorporateBodyHistory corpHist =
                    new CorporateBodyHistory(corpBody);
            corpHist.setText(field678.concatAllBut(""));

            corpBody.getHistories().add(corpHist);
        }

        /*
         * -- note
         */
        // public: 670 (R)
        for (MarcDataField field670 : authRec.getDataFields("670")) {

            final CorporateBodyNote corpNote = new CorporateBodyNote(corpBody);
            corpNote.setText(field670.concatAllBut(""));
            corpNote.setAvailability("public");

            corpBody.getNotes().add(corpNote);
        }

        // private: 667 (R)
        for (MarcDataField field667 : authRec.getDataFields("667")) {

            final CorporateBodyNote corpNote = new CorporateBodyNote(corpBody);
            corpNote.setText(field667.concatAllBut(""));
            corpNote.setAvailability("private");

            corpBody.getNotes().add(corpNote);
        }
    }
}
