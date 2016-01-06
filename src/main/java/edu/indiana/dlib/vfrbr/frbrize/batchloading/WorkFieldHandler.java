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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.WorkMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;

import edu.indiana.dlib.vfrbr.persist.dao.CorporateBodyDAO;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.dao.PersonDAO;
import edu.indiana.dlib.vfrbr.persist.dao.ResponsiblePartyDAO;
import edu.indiana.dlib.vfrbr.persist.dao.WorkDAO;

import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;
import edu.indiana.dlib.vfrbr.persist.relation.WorkHasComposer;
import edu.indiana.dlib.vfrbr.persist.relation.WorkHasCreator;

import javax.persistence.EntityTransaction;
import org.apache.log4j.Logger;

/**
 *  Handle a work MarcDataField.
 */
public class WorkFieldHandler {

    private static Logger log = Logger.getLogger(WorkFieldHandler.class);

    private final DAOFactory daoFactory;

    private final WorkDAO workDAO;

    private final ResponsiblePartyDAO responsiblePartyDAO;

    private final PersonDAO personDAO;

    private final CorporateBodyDAO corporateDAO;

    private final Counts count;

    private final FindPersisted findPersisted;

    private final MarcRecord marcBibRec;

    private final WorkExpressionHandler workExprHandler;

    private final ManifestationJpa marcRecManif;

    /**
     *  Instantiate a WorkFieldHandler
     * for the context of repeated invocations of handleWorkField().
     *
     * @param daoFactory a DAOFactory holding the persistence context.
     * @param marcRecord the current MarcRecord being processed.
     * Source of the MarcDataField work fields to be processed
     * by handleWorkField().
     * @param marcRecManifestation the ManifestationJpa for this MarcRecord.
     */
    public WorkFieldHandler(final DAOFactory daoFactory,
                            final MarcRecord marcBibRecord,
                            final ManifestationJpa marcRecManifestation,
                            final Counts count) {
        /*
         * Establish instance-level context for reuse
         * by repeated invocations of handleWorkField().
         */

        this.daoFactory = daoFactory;
        this.workDAO = daoFactory.newWorkDAO();
        this.responsiblePartyDAO = daoFactory.newResponsiblePartyDAO();
        this.personDAO = daoFactory.newPersonDAO();
        this.corporateDAO = daoFactory.newCorporateBodyDAO();
        this.count = count;
        this.findPersisted = new FindPersisted(count);

        this.marcBibRec = marcBibRecord;

        this.workExprHandler = new WorkExpressionHandler(daoFactory, count);

        this.marcRecManif = marcRecManifestation;
    }

    /**
     *  Handle a MarcDataField work field.
     * @param marcWorkField the MarcDataField work field.
     * @param count counts to increment.
     */
    public final void handleWorkField(final WorkField workField) {
        WorkJpa work;

        final MarcDataField marcWorkField = workField.getWorkDataField();

        final WorkMapper workMapper =
                new WorkMapper(marcWorkField, this.marcBibRec);

        /*
         * to truly match against persisted work instances,
         * use a fully mapped work
         */
        WorkJpa candidateWork = this.workDAO.getNew();
        if (log.isInfoEnabled()) {
            log.info(" -- mapping candidate work for matching");
        }
        MarcRecord workAuthRec = workMapper.mapWork(candidateWork);

        if (log.isInfoEnabled()) {
            log.info("Handling work: " + candidateWork.getAuthIdent());
        }

        final int alreadyLoadedWorks =
                this.workDAO.countByAuthIdent(candidateWork.getAuthIdent());

        if (alreadyLoadedWorks > 0) {
            if (log.isInfoEnabled()) {
                log.info(" -- found already persisted by workAuthIdent");
            }

            if (alreadyLoadedWorks > 1) {
                log.warn("  *** Multiple persisted matches for work: "
                        + candidateWork.getAuthIdent());
            }
            // use first one in list
            work = this.workDAO.getByAuthIdent(candidateWork.getAuthIdent()).
                    get(0);

        } else {

            EntityTransaction entran =
                    this.daoFactory.getEntityManager().getTransaction();
            entran.begin();

            if (log.isInfoEnabled()) {
                log.info(" -- not found persisted, persisting new work");
            }

            // no persisted matches, create a new one
            work = candidateWork;

            // map the current marcBibRec's attributes for the new work
            String workAuthRecId = null;
            if (null != workAuthRec) {
                workAuthRecId = workAuthRec.getControlNumber();
                // if a workAuthRec but no authRecId, use empty string
                if (null == workAuthRecId) {
                    // FIXME use secondary control number
                    workAuthRecId = "";
                }
            }

            /*
             * process Work-Responsible Relationships
             */
            // first, consider by group
            String marcRecGroup = this.marcBibRec.getGroup();

            if (MarcRecord.GROUP1A.equals(marcRecGroup)) {
                /**
                 * 1.1:
                 * link 100-field person as createdBy, composer
                 */
                for (MarcDataField person100Field :
                        this.marcBibRec.getDataFields("100")) {
                    linkPersonComposer(work, person100Field, count);
                }
                /**
                 * 1.1 extension:
                 * link (110, 111)-field corporateBody as createdBy, composer
                 */
                String[] corpFields = {"110", "111"};
                for (MarcDataField corp1xxField :
                        this.marcBibRec.getDataFields(corpFields)) {
                    linkCorporateComposer(work, corp1xxField, count);
                }
                /**
                 * 1.2
                 * if a 700 |4lbt or a 700 |4lyr
                 */
                for (MarcDataField person700Field :
                        this.marcBibRec.getDataFields("700")) {
                    for (String relatorCode :
                            person700Field.getValueList('4')) {
                        if ("lbt".equals(relatorCode)) {
                            /**
                             * 1.2.1
                             * link 700 |4lbt person as createdBy, librettist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "librettist",
                                              count);
                        } else if ("lyr".equals(relatorCode)) {
                            /**
                             * 1.2.2
                             * link 700 |4lyr person as createdBy, lyricist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "lyricist",
                                              count);
                        }
                    }
                }
            } else if (MarcRecord.GROUP1B.equals(marcRecGroup)) {
                /**
                 * 2.1
                 * for (only) works from 245
                 */
                if ("245".equals(marcWorkField.getTag())) {
                    /**
                     * 2.1.1
                     * link 100-field person as createdBy, composer
                     */
                    for (MarcDataField person100Field :
                            this.marcBibRec.getDataFields("100")) {
                        linkPersonComposer(work, person100Field, count);
                    }
                }
                /**
                 * 2.2
                 * if a 700 |4lbt or a 700 |4lyr
                 */
                for (MarcDataField person700Field :
                        this.marcBibRec.getDataFields("700")) {
                    for (String relatorCode :
                            person700Field.getValueList('4')) {
                        if ("lbt".equals(relatorCode)) {
                            /**
                             * 2.2.1
                             * link 700 |4lbt person as createdBy, librettist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "librettist",
                                              count);
                        } else if ("lyr".equals(relatorCode)) {
                            /**
                             * 2.2.2
                             * link 700 |4lyr person as createdBy, lyricist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "lyricist",
                                              count);
                        }
                    }
                }
            } else if (MarcRecord.GROUP1C.equals(marcRecGroup)) {
                /*
                 * don't create any relationships
                 * for this group
                 */
            } else if ((MarcRecord.GROUP2.equals(marcRecGroup))
                    || (MarcRecord.GROUP3.equals(marcRecGroup))
                    || (MarcRecord.GROUP4.equals(marcRecGroup))) {
                /**
                 * 4.1
                 * for Works from 240 or 245
                 */
                if ("240".equals(marcWorkField.getTag())
                        || "245".equals(marcWorkField.getTag())) {
                    /**
                     * 4.1.1
                     * link 100-field person as createdBy, composer
                     */
                    for (MarcDataField person100Field :
                            this.marcBibRec.getDataFields("100")) {
                        linkPersonComposer(work, person100Field, count);
                    }
                } //
                /**
                 * 4.2
                 * for Works from 700 |t
                 */
                else if ("700".equals(marcWorkField.getTag())) {
                    /**
                     * 4.2.1
                     * link this (work) 700-field person as createdBy, composer
                     */
                    linkPersonComposer(work, marcWorkField, count);
                    // TODO could 700 |t work have a corporateBody ???
                }
                /**
                 * 4.3
                 * for all Works in these groups
                 * if a 700 |4lbt or a 700 |4lyr
                 */
                for (MarcDataField person700Field :
                        this.marcBibRec.getDataFields("700")) {
                    for (String relatorCode :
                            person700Field.getValueList('4')) {
                        if ("lbt".equals(relatorCode)) {
                            /**
                             * 4.3.1
                             * link 700 |4lbt person as createdBy, librettist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "librettist",
                                              count);
                        } else if ("lyr".equals(relatorCode)) {
                            /**
                             * 4.3.2
                             * link 700 |4lyr person as createdBy, lyricist
                             */
                            linkPersonCreator(work,
                                              person700Field,
                                              "lyricist",
                                              count);
                        }
                    }
                }
            }

            // persist work for db id
            this.workDAO.persist(work);

            entran.commit();

            entran = this.daoFactory.getEntityManager().getTransaction();
            entran.begin();

            // persist data for frbrization reports for Work
            this.workDAO.reportWorks(work,
                                     this.marcBibRec.getGroup().toString(),
                                     workField.getWorkIdentAlgorithm(),
                                     this.marcBibRec.getControlNumber(),
                                     marcWorkField.getTag(),
                                     workAuthRecId,
                                     count.getFileName(),
                                     count.getRecNum());

            entran.commit();
            this.count.incrementPersistedWorks();
        }

        /*
         * now have work, either pre-existing or newly created
         * for either case,
         *   process for adding expression and manifestation
         */
        EntityTransaction entran =
                this.daoFactory.getEntityManager().getTransaction();
        entran.begin();

        this.workExprHandler.handleWorkExpression(this.marcBibRec,
                                                  work,
                                                  this.marcRecManif);

        if (log.isInfoEnabled()) {
            // some works don't have uniformTitle,
            // but if they do it should be stored first, so...
            log.info(" ---- Saving Work \""
                    + work.getTitles().get(0).getText() + "\"");
        }

        // persist updates
        this.workDAO.persist(work);
        entran.commit();
    }

    /**
     *  Link a person to a work by createdBy as composer.
     * @param work the WorkJpa work from which to link.
     * @param personField a 100 or 700 MarcDataField holding the person reference.
     * @param count counts to increment.
     */
    private void linkPersonComposer(final WorkJpa work,
                                    final MarcDataField personField,
                                    final Counts count) {
        // look for persisted person
        // matching the composer reference in the personField

        final PersonJpa personComposer =
                this.findPersisted.person(personField,
                                          "composer",
                                          this.personDAO);

        if (personComposer != null) {
            // found, create relationship
            final WorkHasComposer workHasComposer =
                    new WorkHasComposer();
            workHasComposer.setSourceWork(work);
            workHasComposer.setTargetResponsibleParty(personComposer);
            // source backlink
            work.getHasComposerPersons().add(workHasComposer);
            // target backlink
            personComposer.getIsComposerOfWorks().add(workHasComposer);

            if (log.isInfoEnabled()) {
                log.info("      -- composer linked");
            }
        } else {
            // mo matching Person for composer
            count.incrementUnmatchedComposers();
            if (log.isInfoEnabled()) {
                log.info("      -- composer not linked");
            }
        }
    }

    private void linkPersonCreator(final WorkJpa work,
                                   final MarcDataField personField,
                                   final String role,
                                   final Counts count) {
        // look for persisted person
        // matching the creator reference in the personField

        final PersonJpa personCreator =
                this.findPersisted.person(personField,
                                          "creator",
                                          this.personDAO);

        if (personCreator != null) {
            // found, create relationship
            final WorkHasCreator workHasCreator =
                    new WorkHasCreator();
            workHasCreator.setSourceWork(work);
            workHasCreator.setTargetResponsibleParty(personCreator);
            workHasCreator.setRelRole(role);
            // source backlink
            work.getHasCreatorPersons().add(workHasCreator);
            // target backlink
            personCreator.getIsComposerOfWorks().add(workHasCreator);

            if (log.isInfoEnabled()) {
                log.info("      -- creator linked");
            }
        } else {
            // not found
            if (log.isInfoEnabled()) {
                log.info("      -- creator not linked");
            }
            this.count.incrementUnmatchedCreators();
        }
    }

    /*
     * -- Note:
     * person and corporateBody are processed separately
     * because of the different combinations of subfields used
     * in persistence and identification.  Thus there are separate
     * mappers for person and corporateBody.
     */
    private void linkCorporateComposer(final WorkJpa work,
                                       final MarcDataField corporateField,
                                       final Counts count) {
        // look for persisted corporateBody
        // matching the composer reference in the corporateField

        final CorporateBodyJpa corporateComposer =
                this.findPersisted.corporateBody(corporateField,
                                                 "composer",
                                                 this.corporateDAO);

        if (corporateComposer != null) {
            // found, create relationship
            final WorkHasComposer workHasComposer =
                    new WorkHasComposer();
            workHasComposer.setSourceWork(work);
            workHasComposer.setTargetResponsibleParty(corporateComposer);
            // source backlink
            work.getHasComposerCorporations().add(workHasComposer);
            // target backlink
            corporateComposer.getIsComposerOfWorks().add(workHasComposer);

            if (log.isInfoEnabled()) {
                log.info("      -- composer  linked");
            }
        } else {
            // mo matching CorporateBody for composer
            count.incrementUnmatchedComposers();
            if (log.isInfoEnabled()) {
                log.info("      -- composer not linked");
            }
        }
    }
}
