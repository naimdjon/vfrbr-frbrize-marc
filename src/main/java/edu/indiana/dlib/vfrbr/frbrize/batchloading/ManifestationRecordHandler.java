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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.CorporateBodyDAO;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.dao.ManifestationDAO;
import edu.indiana.dlib.vfrbr.persist.dao.PersonDAO;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import edu.indiana.dlib.vfrbr.persist.relation.ManifestationToResponsibleParty;

import java.util.List;

import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

/**
 *  Handle the second Manifestion pass,
 * primarily the "producedBy" relationship associations.
 *
 */
public class ManifestationRecordHandler {

    private static Logger log =
            Logger.getLogger(ManifestationRecordHandler.class);

    private final DAOFactory daoFactory;

    private final PersonDAO personDAO;

    private final CorporateBodyDAO corporateDAO;

    private final ManifestationDAO manifDAO;

    private final Counts count;

    private final FindPersisted findPersisted;

    private int persListOrder = 0;

    private int corpListOrder = 0;

    /**
     *  Instantiate a new ManifestationRecordHandler.
     * Initialize.
     * @param daoFactory DAOFactory holding persitence context.
     */
    public ManifestationRecordHandler(final DAOFactory daoFactory,
                                      final Counts count) {

        this.daoFactory = daoFactory;
        this.manifDAO = daoFactory.newManifestationDAO();
        this.personDAO = daoFactory.newPersonDAO();
        this.corporateDAO = daoFactory.newCorporateBodyDAO();
        this.count = count;
        this.findPersisted = new FindPersisted(count);
    }

    /**
     *  Process an already created ManifestationJpa against
     * a MarcRecord.
     * @param marcBibRec MarcRecord holding the manifestation.
     * @param recManif the ManifestationJpa being processed.
     * @param manifWorkCount int value of work records for this manifestation.
     */
    public final void handleManifestationRecord(
            final MarcRecord marcBibRec,
            final ManifestationJpa recManif,
            final int manifWorkCount) {

        final String bibRecGroup = marcBibRec.getGroup();

        EntityTransaction entran =
                this.daoFactory.getEntityManager().getTransaction();
        entran.begin();

        if (log.isInfoEnabled()) {
            log.info("Handling linkages for manifestation \""
                    + recManif.getTitles().get(0).getText()
                    + "\"");
            log.info("    -- bib record group type: " + bibRecGroup);
            log.info("    -- work count: " + manifWorkCount);
        }

        if (bibRecGroup.equals(MarcRecord.GROUP1A)) {
            if (manifWorkCount > 0) {
                /**
                 * 1.1
                 * If at least one work is identified
                 * link non-realizer persons
                 */
                linkNonRealizerPersons(marcBibRec, recManif);
            } else {
                /**
                 * 1.2
                 * If no work is identified
                 * link all 100, 110, 700, 710 associates
                 */
                linkNonWorkAssociates(marcBibRec, recManif);
            }
            /**
             * 1.3
             * (regardless of works or not)
             * link 711 (meeting)
             */
            if (marcBibRec.hasField("711")) {
                linkProducerCorporation(marcBibRec.getDataField("711"),
                                        recManif);
            }
        } else if (bibRecGroup.equals(MarcRecord.GROUP1B)) {
            if (manifWorkCount > 0) {
                /**
                 * 2.1
                 * If at least one work is identified
                 * link non-realizer persons
                 */
                linkNonRealizerPersons(marcBibRec, recManif);

            } else {
                /**
                 * 2.2
                 * If no work is identified
                 * link all 100, 110, 700, 710 associates
                 */
                linkNonWorkAssociates(marcBibRec, recManif);
            }
            /**
             * 2.3
             * (regardless of works or not)
             * link all 111, 711
             */
            final String[] fields2Dot3 = {"111", "711"};
            for (MarcDataField corporateField :
                    marcBibRec.getDataFields(fields2Dot3)) {

                linkProducerCorporation(corporateField, recManif);
            }
        } else if (bibRecGroup.equals(MarcRecord.GROUP1C)) {
            /**
             * 3.1
             * link all 700, 710, 711
             */
            final String[] corpFieldTags = {"710", "711"};
            // people
            for (MarcDataField personField :
                    marcBibRec.getDataFields("700")) {
                linkProducerPerson(personField, recManif);
            }
            // corporations
            for (MarcDataField corporateField :
                    marcBibRec.getDataFields(corpFieldTags)) {
                linkProducerCorporation(corporateField, recManif);
            }
        } else if ((MarcRecord.GROUP2.equals(bibRecGroup))
                || (MarcRecord.GROUP3.equals(bibRecGroup))
                || (MarcRecord.GROUP4.equals(bibRecGroup))) {
            if (manifWorkCount > 0) {
                /**
                 * 4.1
                 * If at least one work is identified
                 * link non-realizer persons
                 */
                linkNonRealizerPersons(marcBibRec, recManif);
            } else {
                /**
                 * 4.2
                 * If no work is identified
                 * link all 100, 110, 700, 710 associates
                 */
                linkNonWorkAssociates(marcBibRec, recManif);

            }
            /**
             * 4.3
             * (regardless of works or not)
             * link all 111, 711
             */
            final String[] fieldTags = {"111", "711"};
            for (MarcDataField corporateField :
                    marcBibRec.getDataFields(fieldTags)) {
                linkProducerCorporation(corporateField, recManif);
            }
        } else {
            // unexpected group condition
        }

        this.manifDAO.persist(recManif);

        entran.commit();

        entran = this.daoFactory.getEntityManager().getTransaction();
        entran.begin();

        // persist data for report on manif with no works
        if (manifWorkCount == 0) {

            this.manifDAO.reportManifNoWork(recManif,
                                            marcBibRec.getControlNumber(),
                                            marcBibRec.getGroup(),
                                            count.getFileName(),
                                            count.getRecNum());
        }

        entran.commit();
        this.count.incrementPersistedManifestations();
    }

    /**
     *  Link non-realizer persons.
     * @param marcBibRec the MarcRecord.
     * @param recManif and current ManifestationJpa.
     */
    private void linkNonRealizerPersons(final MarcRecord marcBibRec,
                                        final ManifestationJpa recManif) {
        if (log.isInfoEnabled()) {
            log.info(" -- linking producers:");
        }

        // For every 7xx [that might identify a person]
        for (MarcDataField personField :
                marcBibRec.getDataFields("700")) {
            // and no |t and no |4(prf, cnd, lbt, lyr)
            if (!personField.hasSubfields("t".toCharArray())) {
                // and
                if (personField.hasSubfields("4".toCharArray())) {
                    // has value(s) for |4 (R)
                    for (String relatorCode : personField.getValueList('4')) {

                        if ((Roles.isRecognizedRoleCode(relatorCode)
                             && (!Roles.isRealizerRoleCode(relatorCode)))) {

                            // |t and no excluded |4 values, link person
                            linkProducerPerson(personField, recManif);
                        }
                    }
                } else {
                    // no |t and no |4, link person

                    linkProducerPerson(personField, recManif);
                }
            }
            // else contains a |t, don't link person
        }
    }

    /**
     *  Link non-work associated persons and corporations.
     * @param marcRecord the MarcRecord.
     * @param recManif the current manifestation.
     */
    private void linkNonWorkAssociates(final MarcRecord marcRecord,
                                       final ManifestationJpa recManif) {
        if (log.isInfoEnabled()) {
            log.info(" -- linking non-work producers:");
        }

        // Else (if no work is identified) take all 100, 110, 700, 710
        final String[] persFieldTags = {"100", "700"};
        final String[] corpFieldTags = {"110", "710"};

        for (MarcDataField personField :
                marcRecord.getDataFields(persFieldTags)) {

            linkProducerPerson(personField, recManif);
        }

        for (MarcDataField corporationfield :
                marcRecord.getDataFields(corpFieldTags)) {

            linkProducerCorporation(corporationfield, recManif);
        }
    }

    /**
     *  Link producedBy person.
     * @param persDataField MarcDataField for person.
     * @param recManif current ManifestionJpa.
     */
    private void linkProducerPerson(final MarcDataField persDataField,
                                    final ManifestationJpa recManif) {

        // look for persisted person
        // matching the person reference in the personDataField
        final PersonJpa producerPerson =
                this.findPersisted.person(persDataField,
                                          "producer",
                                          this.personDAO);

        if (null != producerPerson) {
            // found, create link
            final ManifestationToResponsibleParty producedBy =
                    new ManifestationToResponsibleParty(recManif,
                                                        producerPerson,
                                                        this.persListOrder++,
                                                        "producedBy");
            // if there are any relator codes, use the first one for role
            final String relatorCode = persDataField.getValue('4');
            if (Roles.isRecognizedRoleCode(relatorCode)) {
                producedBy.setRelRole(Roles.getRelatorCodeName(relatorCode));
            }

            // link source and target to relation
            recManif.getHasProducerPersons().add(producedBy);
            producerPerson.getIsProducerOfManifestations().add(producedBy);

            if (log.isInfoEnabled()) {
                log.info("    -- producer linked");
            }
        } else {
            // not found
            if (log.isInfoEnabled()) {
                log.info("    -- producer not linked");
            }
            this.count.incrementUnmatchedProducers();
        }
    }

    /**
     *  Link producedBy corporateBody.
     * @param corpDataField MarcDataField with corporateBody.
     * @param recManif current ManifestationJpa.
     */
    private void linkProducerCorporation(final MarcDataField corpDataField,
                                         final ManifestationJpa recManif) {

        // look for persisted corporateBody
        // matching the reference in the corporateDataField
        final CorporateBodyJpa producerCorp =
                findPersisted.corporateBody(corpDataField,
                                            "producer",
                                            this.corporateDAO);
        if (null != producerCorp) {
            // found, create relationship
            final ManifestationToResponsibleParty producedBy =
                    new ManifestationToResponsibleParty(recManif,
                                                        producerCorp,
                                                        this.corpListOrder++,
                                                        "producedBy");
            // if there are any relator codes, use the first one for role
            final String relatorCode = corpDataField.getValue('4');
            if (Roles.isRecognizedRoleCode(relatorCode)) {
                producedBy.setRelRole(Roles.getRelatorCodeName(relatorCode));
            }

            // link source and target to relation
            recManif.getHasProducerCorporateBodies().add(producedBy);
            producerCorp.getIsProducerOfManifestations().add(producedBy);

            if (log.isInfoEnabled()) {
                log.info("    -- producer linked");
            }
        } else {
            // not found
            if (log.isInfoEnabled()) {
                log.info("    -- producer not linked");
            }
            this.count.incrementUnmatchedProducers();
        }
    }
}
