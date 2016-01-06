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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.PersonMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;

import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.dao.PersonDAO;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

/**
 *  Handler for person field of a MarcRecord.
 */
public class PersonFieldHandler {

    private static Logger log = Logger.getLogger(PersonFieldHandler.class);

    final private DAOFactory daoFactory;

    final private PersonDAO personDAO;

    final private Counts count;

    final private FindPersisted findPersisted;

    /**
     *  Instantiate a new PersonFieldHander.
     * @param daoFactory a DAOFactory holding the persistence context.
     */
    public PersonFieldHandler(final DAOFactory daoFactory,
                              final Counts count) {

        this.daoFactory = daoFactory;
        this.personDAO = daoFactory.newPersonDAO();
        this.count = count;
        this.findPersisted = new FindPersisted(count);
    }

    /**
     *  Handle a single person field of a MarcRecord.
     * @param personField
     * @param count
     */
    public final void handlePersonField(final MarcDataField personField,
                                        final String bibRecIdent) {

        if (log.isInfoEnabled()) {
            log.info("Handling person: "
                    + PersonMapper.getAuthIdent(personField));
        }

        if (isAlreadyLoadedPerson(personField)) {
            // already loaded
            // and said so below, by case
        } else {

            final PersonMapper personMapper = new PersonMapper();

            final AuthorityHandler authHandler = new AuthorityHandler();

            final MarcRecord authRecord =
                    authHandler.getAuthorityPersonRecord(personField);

            if (authRecord == null) {
                // no authority record, build from MarcDataField

                EntityTransaction entran =
                        this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                if (log.isInfoEnabled()) {
                    log.info("  -- creating from the bib field.");
                }

                final PersonJpa person = this.personDAO.getNew();

                personMapper.mapFromBibField(personField, person);
                this.personDAO.persist(person);

                entran.commit();
                entran = this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                this.personDAO.reportG2Bib(person,
                                           bibRecIdent,
                                           personField.getTag(),
                                           personField.toString());
                entran.commit();
                this.count.incrementPersistedPersons();

            } else {
                // authority record, build from
                EntityTransaction entran =
                        this.daoFactory.getEntityManager().getTransaction();
                entran.begin();

                if (log.isInfoEnabled()) {
                    log.info(
                            "  -- creating from matching cached authority record.");
                    log.info("---- MARC Auth record:\n"
                            + authRecord.toString()
                            + "----");
                }

                if (log.isDebugEnabled()) {
                    log.debug("     db person count before creating: "
                            + this.personDAO.countAll());
                }

                final PersonJpa person = this.personDAO.getNew();

                personMapper.mapFromAuthRecord(authRecord, person);
                this.personDAO.persist(person);
                entran.commit();
                this.count.incrementPersistedPersons();

                if (log.isDebugEnabled()) {
                    log.debug("     db person count after creating: "
                            + this.personDAO.countAll());
                }

            }
        }
    }

    /**
     *  Is the Person in a MarcDataField already persisted?
     * @param personField the MarcDataField holding a Person.
     * @return whether the person is already loaded.
     */
    private boolean isAlreadyLoadedPerson(final MarcDataField personField) {

        boolean alreadyLoaded = false;

        if (null != this.findPersisted.person(personField,
                                              "referenced",
                                              this.personDAO)) {
            alreadyLoaded = true;
        }

        return alreadyLoaded;
    }
}
