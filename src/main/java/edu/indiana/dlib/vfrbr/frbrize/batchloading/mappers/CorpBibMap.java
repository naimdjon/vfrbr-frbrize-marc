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
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyDate;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyName;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyNumber;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyPlace;

/**
 *  Mapping of a corporateBody from a bibliographic MarcDataField.
 *
 */
final class CorpBibMap {

    /**
     * Map from a bibliographic MarcDataFieldto a CorporateBodyJpa.
     *
     * @param bibCorpField a 110, 111, 610, 611, 710, or 711
     * MarcDataField for a CorporateBody.
     * @param corpBody the CorporateBodyJpa instance to be mapped.
     */
    protected void mapBibField(final MarcDataField bibCorpField,
                               final CorporateBodyJpa corpBody) {

        /*
         * -- authIdent
         */
        corpBody.setAuthIdent(CorporateBodyMapper.getAuthIdent(bibCorpField));

        /*
         * -- nameOfTheCorporateBody
         */
        // 110,111,610,611,710,711
        // |a (NR) |b (R) |e (R)
        if (bibCorpField.hasSubfields("abe".toCharArray())) {
            final CorporateBodyName corpName =
                    new CorporateBodyName(corpBody);
            corpName.setText(bibCorpField.concatSubfields("abe".toCharArray()));
            corpName.setType("authorized");
            corpName.setVocabulary("naf");
            corpName.setNormal(CorporateBodyMapper.getNormalName(bibCorpField));

            corpBody.getNames().add(corpName);
            corpBody.setAuthorizedName(corpName.getText());
        }

        /*
         * -- numberAssociatedWithTheCorporatebody
         */
        // 110,111,610,611/710,711
        // |n (R)
        if (!bibCorpField.hasSubfields("kt".toCharArray())) {
            for (String numVal : bibCorpField.getValueList('n')) {
                final CorporateBodyNumber corpNum =
                        new CorporateBodyNumber(corpBody);
                corpNum.setText(numVal);

                corpBody.getNumbers().add(corpNum);
            }
        }

        /*
         * -- placeAssociatedWithTheCorporateBody
         */
        // 110,111,610,611,710,711
        // |c (NR)
        if (bibCorpField.hasSubfields("c".toCharArray())) {
            final CorporateBodyPlace corpPlace =
                    new CorporateBodyPlace(corpBody);
            corpPlace.setText(bibCorpField.getValue('c'));

            corpBody.getPlaces().add(corpPlace);
        }

        /*
         * -- dateAssociatedWithTheCorporateBody
         */
        // 110,111,610,711,710,711
        // |d (R)
        for (String dateVal : bibCorpField.getValueList('d')) {
            final CorporateBodyDate corpDate =
                    new CorporateBodyDate(corpBody);
            DateNormalizer.normalizeCorporateDate(dateVal, corpDate);

            corpBody.getDates().add(corpDate);
        }

        /*
         * -- otherDesignationAssociatedWithTheCorporateBody
         */
        // no mapping from bib recs

        /*
         * -- languageOfTheCorporateBody
         */
        // no mapping from bib recs

        /*
         * -- address
         */
        // no mapping from bib recs

        /*
         * -- fieldOfActivity
         */
        // no mapping from bib recs

        /*
         * -- history
         */
        // no mapping from bib recs

        /*
         * -- note
         */
        // no mapping from bib recs
    }
}
