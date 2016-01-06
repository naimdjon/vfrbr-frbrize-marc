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

import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonBiography;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonDate;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonDesignation;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonName;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonNote;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonTitle;

import java.util.List;

/**
 *  Implementation mapping of a Person from an authority MarcRecord.
 *
 */
final class PersAuthMap {

    /**
     * Map from authority MarcRecord to PersonJpa person.
     *
     * @param authRec authority MarcRecord.
     * @param person PersonJpa person to map
     */
    protected void mapAuthRec(final MarcRecord authRec,
                              final PersonJpa person) {

        final MarcDataField field100 = authRec.getDataField("100");

        // -- nameOfPerson
        //
        // -- authorized name: 100 (NR)
        if (null != field100) {
            // to authorizedName
            person.setAuthorizedName(
                    field100.concatSubfields("aq".toCharArray()).trim());
            // to names
            final PersonName persName = new PersonName(person);
            persName.setText(person.getAuthorizedName());
            persName.setType("authorized");
            persName.setVocabulary("naf");
            persName.setNormal(PersonMapper.getNormalName(field100));
            person.getNames().add(persName);
        }

        // -- authIdent
        //      from field100 above
        if (null != field100) {
            person.setAuthIdent(PersonMapper.getAuthIdent(field100));
        }

        // -- variant names: 400 (R)
        final List<MarcDataField> fields400 = authRec.getDataFields("400");
        for (MarcDataField field400 : fields400) {
            final PersonName varName = new PersonName(person);
            varName.setText(
                    field400.concatSubfields("aq".toCharArray()).trim());
            varName.setType("variant");
            varName.setNormal(PersonMapper.getNormalName(field400));
            person.getNames().add(varName);
        }

        // -- datesOfPerson 100 (NR) |d (NR)
        if (null != field100) {
            final String dateString = field100.getValue('d');
            if (null != dateString) {
                final PersonDate persDate = new PersonDate(person);
                DateNormalizer.normalizePersonDate(dateString, persDate);
                person.getDates().add(persDate);
            }
        }

        // -- titleOfPerson 100 (NR) |c (R)
        if (null != field100) {
            final List<String> titleList = field100.getValueList('c');
            for (String title : titleList) {
                final PersonTitle persTitle = new PersonTitle(person);
                persTitle.setText(title);

                person.getTitles().add(persTitle);
            }
        }

        // -- otherDesignation.. 100 (NR) |b (NR)
        if (null != field100) {
            final String desig = field100.getValue('b');
            if (null != desig) {
                final PersonDesignation persDesig =
                        new PersonDesignation(person);
                persDesig.setText(desig);

                person.getDesignations().add(persDesig);
            }
        }

        // -- biographyHistory  678 (R)
        final List<MarcDataField> field678List = authRec.getDataFields("678");
        for (MarcDataField field678 : field678List) {
            final String subfields = field678.concatAllBut("");
            if (null != subfields) {
                final PersonBiography persBiog = new PersonBiography(person);
                persBiog.setText(subfields);

                person.getBiographies().add(persBiog);
            }
        }

        // -- note 670 (R)
        final List<MarcDataField> field670List = authRec.getDataFields("670");
        for (MarcDataField field670 : field670List) {
            final String subfields = field670.concatAllBut("");
            if (null != subfields) {
                final PersonNote persNote = new PersonNote(person);
                persNote.setText(subfields);
                persNote.setAvailability("public");

                person.getNotes().add(persNote);
            }
        }
    }
}
