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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.CorporateBodyMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.CorporateBodyDAO;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import org.apache.log4j.Logger;

import javax.persistence.EntityTransaction;

/**
 *  Handler for CorporateBody field of a MarcRecord.
 */
public class CorporateFieldHandler {

    private static final Logger log =
            Logger.getLogger(CorporateFieldHandler.class);

    private final DAOFactory daoFactory;

    private final CorporateBodyDAO corporateDAO;

    private final Counts count;

    private final FindPersisted findPersisted;

    /**
     *  Handler for corporateBody field.
     * @param daoFactory DAOFactory holding persistence context.
     */
    public CorporateFieldHandler(final DAOFactory daoFactory,
                                 final Counts count) {

        this.daoFactory = daoFactory;
        this.corporateDAO = daoFactory.newCorporateBodyDAO();
        this.count = count;
        this.findPersisted = new FindPersisted(count);
    }

    /**
     *  Handle a single CorporteBody field from a MarcRecord.
     * @param corpBodyField corporateBody MarcDataField to process.
     * @param count counts to increment.
     */
    public final void handleCorporateField(final MarcDataField corpBodyField,
                                           final String bibRecIdent) {

        if (log.isInfoEnabled()) {
            log.info("Handling corporateBody: "
                    + CorporateBodyMapper.getAuthIdent(corpBodyField));
        }

        if (isAlreadyLoadedCorporateBody(corpBodyField)) {
            // already persisted, no need to process this record
            // logged outcome below, by case
        } else {

            final MarcRecord authRecord = new AuthorityHandler().getAuthorityCorporateBodyRecord(corpBodyField);

            if (authRecord == null) {
                //no authority match, build from MARC bibliographic marcRecord
                EntityTransaction entran =
                        this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                if (log.isInfoEnabled()) {
                    log.info("  -- creating from the bib record.");
                }

                final CorporateBodyJpa corpBody = this.corporateDAO.getNew();
                new CorporateBodyMapper().mapFromBibField(corpBodyField,
                                                          corpBody);
                this.corporateDAO.persist(corpBody);

                entran.commit();
                entran = this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                this.corporateDAO.reportG2Bib(corpBody,
                                              bibRecIdent,
                                              corpBodyField.getTag(),
                                              corpBodyField.toString());
                entran.commit();
                this.count.incrementPersistedCorporateBodies();

            } else {
                // authority match, build from cached authority record
                EntityTransaction entran =
                        this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                // TODO consider backup persisted check based on authRec

                if (log.isInfoEnabled()) {
                    log.info(
                            "  -- creating from matching cached authority record.");
                    log.info("---- MARC Auth record:\n"
                            + authRecord.toString()
                            + "----");
                }

                final CorporateBodyJpa corpBody = this.corporateDAO.getNew();
                new CorporateBodyMapper().mapFromAuthRecord(authRecord, corpBody);
                this.corporateDAO.persist(corpBody);
                entran.commit();
                this.count.incrementPersistedCorporateBodies();
            }
        }
    }

    /**
     * Is this corporateBody already persisted.
     * @param corpBodyField corporate field from the MarcRecord.
     * @return persisted or not.
     */
    private boolean isAlreadyLoadedCorporateBody(
            final MarcDataField corpBodyField) {

        boolean alreadyLoaded = false;

        if (null != this.findPersisted.corporateBody(corpBodyField,
                                                     "referenced",
                                                     this.corporateDAO)) {
            alreadyLoaded = true;
        }

        return alreadyLoaded;
    }
}
