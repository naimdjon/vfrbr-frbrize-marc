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
package edu.indiana.dlib.vfrbr.frbrize.batchloading;

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.ExpressionMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;

import edu.indiana.dlib.vfrbr.persist.dao.CorporateBodyDAO;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.dao.ExpressionDAO;
import edu.indiana.dlib.vfrbr.persist.dao.PersonDAO;

import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionJpa;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;

import edu.indiana.dlib.vfrbr.persist.relation.ExpressionToManifestation;
import edu.indiana.dlib.vfrbr.persist.relation.ExpressionToResponsibleParty;
import edu.indiana.dlib.vfrbr.persist.relation.WorkToExpression;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *  Handler for the Expression of a Work.
 */
public class WorkExpressionHandler {

    private static Logger log = Logger.getLogger(WorkExpressionHandler.class);

    private final ExpressionDAO expressionDAO;

    private final PersonDAO personDAO;

    private final CorporateBodyDAO corporateBodyDAO;

    private final Counts count;

    private final FindPersisted findPersisted;

    private int persRealizerListOrder = 0;

    private int corpRealizerListOrder = 0;


    /**
     *  Instantiate a new WorkExpressionHandler.
     * @param daoFactory a DAOFactory holding the persistence context.
     */
    public WorkExpressionHandler(final DAOFactory daoFactory,
                                 final Counts count) {

        this.expressionDAO = daoFactory.newExpressionDAO();
        this.personDAO = daoFactory.newPersonDAO();
        this.corporateBodyDAO = daoFactory.newCorporateBodyDAO();
        this.count = count;
        this.findPersisted = new FindPersisted(count);
    }

    /**
     *  Handle the Expressions of a Work.
     * @param marcBibRec the MarcRecord for the Work.
     * @param work the WorkJpa work.
     * @param marcRecManif the ManifestationJpa for the MarcRecord.
     * @param count counts to increment.
     */
    public final void handleWorkExpression(final MarcRecord marcBibRec,
                                           final WorkJpa work,
                                           final ManifestationJpa marcRecManif) {

        // check the candidate expression from the bibRec
        final ExpressionMapper exprMapper =
                new ExpressionMapper(marcBibRec, work);

        // expressionMapper uses work titles, so...
        log.info("Handle expression \""
                + work.getTitles().get(0).getText()
                + "\"");

        // get any matching persisted expressions
        final List<ExpressionJpa> matchingExprs =
                new ArrayList<ExpressionJpa>();

        // for all the work.hasRealizations expressions
        for (int idx = 0;
                idx < work.getHasRealizations().size();
                idx++) {
            WorkToExpression realizedBy = work.getHasRealizations().get(idx);

            // test for null realizedBy
            if (null == realizedBy) {
                log.warn("****** WARNING ******");
                log.warn("* null realizedBy from work.getHasRealizations() ");
                log.warn("* for work.uniformTitle: \""
                        + work.getUniformTitle() + "\"");
                log.warn("* hasRealizations List size: "
                        + work.getHasRealizations().size());
                log.warn("* this element index: " + idx);
                log.warn("* skipping this element");
                log.warn("*******");
            } else {
                // if the target expression is a match
                if (exprMapper.isMatchingExpression(
                        realizedBy.getTargetExpression())) {
                    // add it to the list
                    matchingExprs.add(realizedBy.getTargetExpression());
                }
            }
        }

        // the ExpressionJpa to use, either matched or new
        ExpressionJpa expr = null;

        if (!matchingExprs.isEmpty()) {
            // at least one match
            if (matchingExprs.size() > 1) {
                // multiple matches, log
                log.info(" -- found multiple matching persisted Expressions:");

                // -- log out the matching expressions
                boolean logExprMatches = true;
                if (logExprMatches) {
                    for (ExpressionJpa exprMatch : matchingExprs) {
                        // id
                        log.info("  -- exprId: " + exprMatch.getId());
                        // title(s)
                        for (int idx = 0;
                                idx < exprMatch.getTitles().size();
                                idx++) {
                            log.info("      Title[" + idx + "] "
                                    + exprMatch.getTitles().get(idx).getText());
                        }
                        // dates(s)
                        if (exprMatch.getDates().size() > 0) {
                            for (int idx = 0;
                                    idx < exprMatch.getDates().size();
                                    idx++) {
                                log.info(
                                        "      Date[" + idx + "] "
                                        + exprMatch.getDates().get(idx).getText());
                            }
                        } else {
                            log.info("      no Dates");
                        }
                        // realizer(s)
                        for (int idx = 0;
                                idx < exprMatch.getHasRealizers().size();
                                idx++) {
                            log.info("      Realizer[" + idx + "] "
                                    + exprMatch.getHasRealizers().get(idx).
                                    getTargetResponsibleParty().getAuthIdent());
                        }
                    }
                }

                log.info(" using first in list");
                expr = matchingExprs.get(0);
            } else {
                // use the first one, in either case
                log.info("  -- found matching persisted Expression");
                expr = matchingExprs.get(0);
            }
        } else {
            // no matching expression, create new one
            log.info("  -- no match, creating new Expression");

            // TODO Should Corp relation include actual realizer roles (vs hardcoded "performer")

            // map the Expression attributes
            expr = exprMapper.getExpression(this.expressionDAO);
            log.info("    -- mapped");

            final String marcRecGroup = marcBibRec.getGroup();

            if (marcRecGroup.equals(MarcRecord.GROUP1A)) {
                /**
                 * 1.1
                 * link 700 persons as realizedBy
                 * for |4 realizer relator codes:
                 */
                // for all the 700 fields in the bibRec
                for (MarcDataField field700 :
                        marcBibRec.getDataFields("700")) {
                    // for all the |4 subField relator codes
                    for (String relatorCode :
                            field700.getValueList('4')) {
                        // if the relator code is a realizer code
                        if (Roles.isRealizerRoleCode(relatorCode)) {
                            /**
                             * link this person with this relator role
                             */
                            linkPersonRealizer(expr,
                                               field700,
                                               Roles.getRelatorCodeName(relatorCode));
                        }
                    }
                }
                /**
                 * 1.2
                 * link 710 corporateBodies as realizedBy, performer
                 * for |4prf
                 */
                for (MarcDataField field710 :
                        marcBibRec.getDataFields("710")) {
                    for (String relatorCode :
                            field710.getValueList('4')) {
                        if ("prf".equals(relatorCode)) {
                            linkCorporateRealizer(expr,
                                                  field710,
                                                  "performer");
                        }
                    }
                }
            } else if (marcRecGroup.equals(MarcRecord.GROUP1B)
                    || marcRecGroup.equals(MarcRecord.GROUP2)
                    || marcRecGroup.equals(MarcRecord.GROUP3)
                    || marcRecGroup.equals(MarcRecord.GROUP4)) {
                /**
                 * 2.1, 4.1
                 * link 100 and 700 persons as realizedBy
                 * for |4 realizer relator codes:
                 */
                // for all the 100, 700 fields in the bibRec
                String[] personFieldTags = {"100", "700"};
                for (MarcDataField personField :
                        marcBibRec.getDataFields(personFieldTags)) {
                    // for all the |4 subField relator codes
                    for (String relatorCode :
                            personField.getValueList('4')) {
                        // if the relator code is a realizer code
                        if (Roles.isRealizerRoleCode(relatorCode)) {
                            /**
                             * link this person with this relator role
                             */
                            linkPersonRealizer(expr,
                                               personField,
                                               Roles.getRelatorCodeName(relatorCode));
                        }
                    }
                }
                /**
                 * 2.2, 4.2
                 * link 110 and 710 |4prf corporateBodies
                 * as realizedBy, performer
                 */
                // 110
                for (MarcDataField corpField :
                        marcBibRec.getDataFields("110")) {
                    linkCorporateRealizer(expr,
                                          corpField,
                                          "performer");
                }
                // 710 |4prf
                for (MarcDataField corpField :
                        marcBibRec.getDataFields("710")) {
                    for (String relatorCode :
                            corpField.getValueList('4')) {
                        if ("prf".equals(relatorCode)) {
                            linkCorporateRealizer(expr,
                                                  corpField,
                                                  "performer");
                        }
                    }
                }
            } else if (marcRecGroup.equals(MarcRecord.GROUP1C)) {
                /**
                 * 3.1
                 * create no realizedBy relationships for this group
                 */
            } else {
                // unknown group condition
                // ? worth logging as a severly unexpected condition ?
            }

            // map realization relationship to work and expression
            // for a newly created expression
            final WorkToExpression realization =
                    new WorkToExpression(work,
                                         expr,
                                         0,
                                         "realizedThrough");

            work.getHasRealizations().add(realization);     // source end
            expr.getIsRealization().add(realization);        // target end
            log.info("    -- realization linked between work and expression");

            this.expressionDAO.persist(expr);
            log.info("    -- Expression persisted");

            this.count.incrementPersistedExpressions();

        } // of create new Expression, for no persisted expression match

        /*
         * now have ExpressionJpa expr, either matched or newly created
         * this MarcRecord manifestation is linked to this expr
         * implicit assertion is that no MarcRecord duplicates an
         * already existing, already persisted, manifestation
         * (new manifestation for every MarcRecord)
         */

        //map embodiment relationship to expression and manifestation
        final ExpressionToManifestation exemplification =
                new ExpressionToManifestation(expr,
                                              marcRecManif,
                                              0,
                                              "embodiedIn");

        expr.getHasEmbodiments().add(exemplification);
        marcRecManif.getIsEmbodiment().add(exemplification);
        log.info(
                "      -- exemplification linked between expression and manifestation");

    }

    private void linkPersonRealizer(final ExpressionJpa expr,
                                    final MarcDataField personDataField,
                                    final String role) {
        // look for persisted person
        // matching the person reference in the personDataField
        final PersonJpa personRealizer =
                this.findPersisted.person(personDataField,
                                          "realizer",
                                          this.personDAO);

        if (personRealizer != null) {
            // found, create relationship
            final ExpressionToResponsibleParty exprHasRealizer =
                    new ExpressionToResponsibleParty();
            exprHasRealizer.setSourceExpression(expr);
            exprHasRealizer.setTargetResponsibleParty(personRealizer);
            exprHasRealizer.setListOrder(this.persRealizerListOrder++);
            exprHasRealizer.setRelRole(role);
            // source backlink
            expr.getHasRealizers().add(exprHasRealizer);
            // target backlink
            personRealizer.getIsRealizerOfExpressions().add(exprHasRealizer);

            if (log.isInfoEnabled()) {
                log.info("      -- realizer linked");
            }
        } else {
            // mo matching Person for realizer
            if (log.isInfoEnabled()) {
                log.info("      -- realizer not linked");
            }
            this.count.incrementUnmatchedRealizers();
        }
    }

    private void linkCorporateRealizer(final ExpressionJpa expr,
                                       final MarcDataField corporateDataField,
                                       final String role) {
        // look for persisted corporateBody
        // matching the reference in the corporateDataField
        final CorporateBodyJpa corporateRealizer =
                this.findPersisted.corporateBody(corporateDataField,
                                                 "realizer",
                                                 this.corporateBodyDAO);

        if (corporateRealizer != null) {
            // found, create relationship
            final ExpressionToResponsibleParty exprHasRealizer =
                    new ExpressionToResponsibleParty();
            exprHasRealizer.setSourceExpression(expr);
            exprHasRealizer.setTargetResponsibleParty(corporateRealizer);
            exprHasRealizer.setListOrder(this.corpRealizerListOrder++);
            exprHasRealizer.setRelRole(role);
            // source backlink
            expr.getHasRealizers().add(exprHasRealizer);
            // target backlink
            corporateRealizer.getIsRealizerOfExpressions().add(
                    exprHasRealizer);

            if (log.isInfoEnabled()) {
                log.info("      -- realizer linked");
            }
        } else {
            // mo matching CorporateBody for realizer
            if (log.isInfoEnabled()) {
                log.info("      -- realizer not linked");
            }
            this.count.incrementUnmatchedRealizers();
        }
    }
}
