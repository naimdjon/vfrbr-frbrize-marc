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
import edu.indiana.dlib.vfrbr.persist.dao.ExpressionDAO;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;
import edu.indiana.dlib.vfrbr.persist.relation.ExpressionToResponsibleParty;

/**
 *  Mapping from MarcRecord to Expression.
 * @author pbmcelwa
 */
public class ExpressionMapper {

    /**
     * The MarcRecord source for the ExpressionJpa mapping.
     */
    private final MarcRecord marcRecord;

    /**
     * The WorkJpa the the ExpressionJpa expresses.
     */
    private final WorkJpa work;

    /**
     *  Create new ExpressionMapper instance.
     *
     * @param marcRecord the source MarcRecord
     * from which to map the ExpressionJpa.
     * @param work the WorkJpa which the ExpressionJpa expresses.
     */
    public ExpressionMapper(final MarcRecord marcRecord,
                            final WorkJpa work) {
        this.marcRecord = marcRecord;
        this.work = work;
    }

    /**
     * Is this persisted Expression parameter a match
     * for the expression definition
     * contained in the MarcRecord of this ExpressionMapper.
     * @param expression the persisted ExpressionJpa to check for matching
     * @return true for is a match, false for not a match
     */
    public final boolean isMatchingExpression(final ExpressionJpa expression) {

        final boolean isDateMatch = isOneDateMatch(expression);

        final boolean isMatch =
                (isDateMatch && ((countMatchingPerson(expression) > 0)
                                 || (countMatchingCorpBody(expression) > 0)))
                || (areTwoMatchingRelators(expression));


        return isMatch;
    }

    /**
     * Does "at least one 033|a matches one dateOfExpression".
     * @param expr ExpressionJpa expression being tested for equality.
     * @return boolean for at least one date matching.
     */
    private boolean isOneDateMatch(final ExpressionJpa expr) {
        boolean isOneMatch = false;

        // 033 (R)
        for (MarcDataField field033 :
                this.marcRecord.getDataFields("033")) {
            // |a (R)
            for (String date033 : field033.getValueList('a')) {
                // expression.dates
                for (int idx = 0;
                        idx < expr.getDates().size();
                        idx++) {
                    // normalize for comparison
                    if (expr.getDates().get(idx).getText().equals(
                            DateNormalizer.normalizeExpr033Date(date033))) {
                        isOneMatch = true;
                    }
                }
            }
        }

        return isOneMatch;
    }

    /**
     * In this ExpressionMapper MarcRecord
     * how many 700 (R) MarcField with a |4 (R) relator code of prf or cnd
     * whose authIdent matches the
     * authIdent of any expression parameter
     * "realizedBy" Person.
     * @param expression persisted ExpressionJpa of realizedBy persons to match
     * @return count of matching persons
     */
    private int countMatchingPerson(final ExpressionJpa expression) {

        int matchCount = 0;

        for (MarcDataField field700 : this.marcRecord.getDataFields("700")) {
            for (String relatorSubfield : field700.getValueList('4')) {
                if ("prf".equals(relatorSubfield)
                        || "cnd".equals(relatorSubfield)) {
                    final String persAuthIdent =
                            PersonMapper.getAuthIdent(field700);
                    // now have a personIdent from the MarcRecord
                    // is there a matching persisted personIdent
                    // under the expression.realizedBy relationship
                    for (ExpressionToResponsibleParty realizedBy :
                            expression.getHasRealizers()) {
                        // this should be a PersonJpa, but ...
                        // TODO better way to screen ResponsiblePartyJpa subclasses?
                        try {
                            final PersonJpa responPers =
                                    (PersonJpa) realizedBy.
                                    getTargetResponsibleParty();
                            if (persAuthIdent.equals(responPers.getAuthIdent())) {
                                matchCount++;
                            }
                        } catch (ClassCastException notPerson) {
                            // not a PersonJpa
                        }
                    }
                }
            }
        }

        return matchCount;
    }

    /**
     * In this ExpressionMapper MarcRecord
     * how many 710 (R) MarcField with |4 (R) relator code of prf
     * whose "authIdent" matches the
     * "authIdent" of any expression parameter
     * "realizedBy" CorporateBody.
     * @param expression ExpressionJpa of realizedBy corporations to match
     * @return count of matching corporateBodies
     */
    private int countMatchingCorpBody(final ExpressionJpa expression) {

        int matchCount = 0;

        for (MarcDataField field710 : this.marcRecord.getDataFields("710")) {
            for (String relatorSubfield : field710.getValueList('4')) {
                if ("prf".equals(relatorSubfield)) {
                    final String corpAuthIdent =
                            CorporateBodyMapper.getAuthIdent(field710);
                    // have a corpIdent from the MarcRecord
                    // is there a matching persisted corpIdent
                    // under the expression.realizedBy relationship
                    for (ExpressionToResponsibleParty realizedBy :
                            expression.getHasRealizers()) {
                        // just want the CorporateBodyJpa ones
                        // TODO a better way to screen ResponsiblePartyJpa subclasses?
                        try {
                            final CorporateBodyJpa corpPers =
                                    (CorporateBodyJpa) realizedBy.
                                    getTargetResponsibleParty();
                            if (corpAuthIdent.equals(corpPers.getAuthIdent())) {
                                matchCount++;
                            }
                        } catch (ClassCastException notCorpBody) {
                            // not a CorporateBodyJpa
                        }
                    }
                }
            }
        }

        return matchCount;
    }

    /**
     * In this ExpressionMapper MarcRecord
     * are there at least two 7XX (R) with any |4 code
     * whose "authIdent" matches the
     * "authIdent" of any expression parameter
     * "realizedBy" ResponsibleParty (Person or CorporateBody).
     * @param expression the ExpressionJpa to compare to the MarcRecord.
     * @return are there at least two relator codes.
     */
    private boolean areTwoMatchingRelators(final ExpressionJpa expression) {
        /*
         * since we are only dealing Person and CorporateBody
         * an assertion is that we are only dealing with 700 and 710 fields
         * and can reuse our counts
         */

        return (countMatchingPerson(expression)
                + countMatchingCorpBody(expression))
                >= 2;
    }

    /**
     * Get an ExpressionJpa from the MarcRecord in this ExpressionMapper.
     * An ExpressionJpa is always returned, even if only newly initialized.
     * @param exprDAO the ExpressionDAO with which
     * to create a new ExpressionJpa.
     * @return a new ExpressionJpa.
     */
    public final ExpressionJpa getExpression(final ExpressionDAO exprDAO) {
        final ExpressionJpa expr = exprDAO.getNew();

        final ExprMap exprMap =
                new ExprMap(marcRecord, work);

        // -- titleOfTheExpression
        // ExpressionTitle titles
        expr.getTitles().addAll(exprMap.mapTitles(expr));

        // -- formOfExpression
        // ExpressionForm forms
        expr.getForms().addAll(exprMap.mapForms(expr));

        // -- dateOfExpression
        // ExpressionDate dates
        expr.getDates().addAll(exprMap.mapDates(expr));

        // -- languageOfExpression
        // ExpressionLanguage languages
        expr.getLanguages().addAll(exprMap.mapLanguages(expr));

        // -- otherDistinguishingCharacteristic
        //  not mapped
        // ExpressionCharacteristic characteristics

        // -- extentOfTheExpression
        // ExpressionExtent extents
        expr.getExtents().addAll(exprMap.mapExtents(expr));

        // -- summarizationOfContent
        //  not mapped
        // ExpressionSummarization summarizations

        // -- contextForTheExpression
        //  not mapped
        // ExpressionContext contexts

        // -- criticalResponseToTheExpression
        //  not mapped
        // ExpressionResponse responses

        // -- typeOfScore
        // ExpressionScoreType scoreType
        expr.getScoreType().addAll(
                exprMap.mapScoreType(expr));

        // -- mediumOfPerformance
        // ExpressionPerformanceMedium performanceMediums
        expr.getPerformanceMediums().addAll(
                exprMap.mapPerformanceMediums(expr));

        // -- note
        // ExpressionNote notes
        expr.getNotes().addAll(exprMap.mapNotes(expr));

        // -- placeOfPerformance
        // ExpressionPerformancePlace performancePlace
        expr.getPerformancePlace().addAll(exprMap.mapPerformancePlace(expr));

        // -- key
        // ExpressionKey keys
        expr.getKeys().addAll(exprMap.mapKeys(expr));

        // -- genreFormStyle
        // ExpressionGenre genres
        expr.getGenres().addAll(exprMap.mapGenres(expr));

        return expr;
    }
}
