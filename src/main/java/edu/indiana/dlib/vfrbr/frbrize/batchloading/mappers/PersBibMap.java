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

import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonDate;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonDesignation;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonName;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonTitle;

import java.util.List;

/**
 *  Implementation mapping of a person from a bibliographic MarcDataField.
 *
 */
final class PersBibMap {

    /**
     * Map from a PersonJpa from a bibliographic MarcDataFieldto.
     *
     * @param personField a MarcDataField for a CorporateBody.
     * @param person the PersonJpa instance to be mapped.
     */
    public void mapBibField(final MarcDataField personField,
                            final PersonJpa person) {

        // -- authIdent
        person.setAuthIdent(PersonMapper.getAuthIdent(personField));

        // -- nameOfPerson |a |q (NR)
        final String nameString =
                personField.concatSubfields("aq".toCharArray());
        if (null != nameString) {
            final PersonName persName = new PersonName(person);
            persName.setText(nameString);
            persName.setType("authorized");
            persName.setVocabulary("naf");
            persName.setNormal(PersonMapper.getNormalName(personField));

            person.getNames().add(persName);
            person.setAuthorizedName(nameString);
        }

        // -- datesOfPerson |d (NR)
        final String dateString = personField.getValue('d');
        if (null != dateString) {
            final PersonDate persDate = new PersonDate(person);
            DateNormalizer.normalizePersonDate(dateString, persDate);
            person.getDates().add(persDate);
        }

        // -- titleOfPerson 100 |c (R)
        final List<String> titleList = personField.getValueList('c');
        for (String title : titleList) {
            final PersonTitle persTitle = new PersonTitle(person);
            persTitle.setText(title);

            person.getTitles().add(persTitle);
        }

        // -- otherDesignation.. |b (NR)
        final String desig = personField.getValue('b');
        if (null != desig) {
            final PersonDesignation persDesig = new PersonDesignation(person);
            persDesig.setText(desig);

            person.getDesignations().add(persDesig);
        }
    }
}
