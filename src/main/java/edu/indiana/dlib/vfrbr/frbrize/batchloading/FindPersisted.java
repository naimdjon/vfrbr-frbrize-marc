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
import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.PersonMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.CorporateBodyDAO;

import edu.indiana.dlib.vfrbr.persist.dao.PersonDAO;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.CorporateBodyJpa;
import edu.indiana.dlib.vfrbr.persist.entity.responsibleparty.PersonJpa;
import java.util.List;

import org.apache.log4j.Logger;

/**
 *  Utility methods for finding matching persisted entities.
 * 
 * @author pbmcelwa
 */
public class FindPersisted {

    private static Logger log = Logger.getLogger(FindPersisted.class);

    private Counts count;

    public FindPersisted(Counts count) {
        this.count = count;
    }

    /**
     *  Find a matching persisted Person.
     * First try authIdent, and then normalName.
     * If no match found, return null.
     *
     * @param persDataField MarcDataField holding the person reference.
     * @param role String of the role for the matching Person.
     * @param personDAO PersonDAO for current persistence context.
     * @return a PersonJpa if matched, otherwise a null PersonJpa reference.
     */
    public final PersonJpa person(MarcDataField persDataField,
                                  String role,
                                  PersonDAO personDAO) {
        PersonJpa personFound = null;

        final String persAuthIdent =
                PersonMapper.getAuthIdent(persDataField);

        if (log.isInfoEnabled()) {
            log.info("   -- seeking persisted "
                    + role
                    + " person by authIdent "
                    + persAuthIdent);
            log.info("      for " + persDataField);
        }

        // first try authIdent
        List<PersonJpa> matchingPersons =
                personDAO.getByAuthIdent(persAuthIdent);

        if (matchingPersons.isEmpty()) {
            // then try normalName
            final String normalName =
                    PersonMapper.getNormalName(persDataField);

            if (log.isInfoEnabled()) {
                log.info("      not found by authIdent, trying normalName "
                        + normalName);
            }

            matchingPersons = personDAO.getByNormalName(normalName);

            if (matchingPersons.isEmpty()) {
                // not found by normalName, either
                if (log.isInfoEnabled()) {
                    log.info("      not found by normalName "
                            + "trying via cached auth record");
                }
                // last resort,
                // see if cached auth record exists
                // whose content does match a persisted entry

                final AuthorityHandler authHandler = new AuthorityHandler();
                final MarcRecord authPersRec =
                        authHandler.getAuthorityPersonRecord(persDataField);

                if (authPersRec == null) {
                    // no cached authority record found
                    if (log.isInfoEnabled()) {
                        log.info(
                                "      no cached auth record found");
                    }
                    // null personFound returned
                } else {
                    // a cached authority record found
                    // see if that authIdent matches a persisted entry
                    PersonJpa authPerson = new PersonJpa();
                    PersonMapper personMapper = new PersonMapper();
                    personMapper.mapFromAuthRecord(authPersRec, authPerson);
                    matchingPersons =
                            personDAO.getByAuthIdent(authPerson.getAuthIdent());
                    if (matchingPersons.isEmpty()) {
                        // not found by authPerson authIdent (??!!)
                        if (log.isInfoEnabled()) {
                            log.info(
                                    "      not found by authRec.");
                        }
                        // null personFound returned
                    } else {
                        // matching person(s) from authPers, just use first
                        personFound = matchingPersons.get(0);
                        if (log.isInfoEnabled()) {
                            log.info(
                                    "      found by authRec.");
                        }
                    }
                }

            } else {
                // one or more found by normalName
                personFound = matchingPersons.get(0);
                if (log.isInfoEnabled()) {
                    log.info("      found by normalName.");
                }
                if (matchingPersons.size() > 1) {
                    // more than one,  warn
                    if (log.isInfoEnabled()) {
                        log.info("      "
                                + matchingPersons.size()
                                + " multiple matches found.");
                    }
                }
            }
        } else {
            // one or more found by authIdent
            personFound = matchingPersons.get(0);
            if (log.isInfoEnabled()) {
                log.info("      found by authIdent.");
            }
            if (matchingPersons.size() > 1) {
                // more than one,  warn
                if (log.isInfoEnabled()) {
                    log.info("      "
                            + matchingPersons.size()
                            + " multiple matches found.");
                }
            }
        }

        return personFound;
    }

    /**
     *  Find a matching persisted CorporateBody.
     * First try authIdent, and then normalName.
     * If no match found, return null.
     *
     * @param corpDataField MarcDataField holding the CorporateBody reference.
     * @param role String role of the matching CorporateBody.
     * @param corporateDAO CorporateBodyDAO for the current persistence context.
     * @return the matching CorporateBodyJpa or a null reference if not matched.
     */
    public final CorporateBodyJpa corporateBody(MarcDataField corpDataField,
                                                String role,
                                                CorporateBodyDAO corporateDAO) {
        CorporateBodyJpa corporateFound = null;

        final String corpAuthIdent =
                CorporateBodyMapper.getAuthIdent(corpDataField);

        if (log.isInfoEnabled()) {
            log.info("   -- seeking persisted "
                    + role
                    + " corporateBody by authIdent "
                    + corpAuthIdent);
            log.info("      for " + corpDataField);
        }

        // first try authIdent
        List<CorporateBodyJpa> matchingCorporates =
                corporateDAO.getByAuthIdent(corpAuthIdent);

        if (matchingCorporates.isEmpty()) {
            // then try normalName
            final String normalName =
                    CorporateBodyMapper.getNormalName(corpDataField);

            if (log.isInfoEnabled()) {
                log.info("      not found by authIdent, trying normalName "
                        + normalName);
            }

            matchingCorporates = corporateDAO.getByNormalName(normalName);

            if (matchingCorporates.isEmpty()) {
                // not found by normalName, either
                if (log.isInfoEnabled()) {
                    log.info("      not found by normalName "
                            + "trying via cached auth record");
                }
                // last resort,
                // see if cached auth record exists
                // whose content does match a persisted entry

                final AuthorityHandler authHandler = new AuthorityHandler();
                final MarcRecord authCorpRec =
                        authHandler.getAuthorityCorporateBodyRecord(
                        corpDataField);

                if (authCorpRec == null) {
                    // no cached authority record found
                    if (log.isInfoEnabled()) {
                        log.info(
                                "      no cached auth record found");
                    }
                    // null corporateFound returned
                } else {
                    // a cached authority record found
                    // see if that authIdent matches a persisted entry
                    CorporateBodyJpa authCorp = new CorporateBodyJpa();
                    CorporateBodyMapper corpMapper = new CorporateBodyMapper();
                    corpMapper.mapFromAuthRecord(authCorpRec, authCorp);
                    matchingCorporates =
                            corporateDAO.getByAuthIdent(authCorp.getAuthIdent());
                    if (matchingCorporates.isEmpty()) {
                        // not found by authCorp
                        if (log.isInfoEnabled()) {
                            log.info(
                                    "      not found by authRec.");
                        }
                        // null corporateFound returned
                    } else {
                        // matching corporate(s) from authCorp, use first
                        corporateFound = matchingCorporates.get(0);
                        if (log.isInfoEnabled()) {
                            log.info(
                                    "      found by authRec.");
                        }
                    }
                }

            } else {
                // one or more found by normalName
                corporateFound = matchingCorporates.get(0);
                if (log.isInfoEnabled()) {
                    log.info("      found by normalName.");
                }
                if (matchingCorporates.size() > 1) {
                    // more than one,  warn
                    if (log.isInfoEnabled()) {
                        log.info("      "
                                + matchingCorporates.size()
                                + " multiple matches found.");
                    }
                }
            }
        } else {
            // one or more found by authIdent
            corporateFound = matchingCorporates.get(0);
            if (log.isInfoEnabled()) {
                log.info("      found by authIdent.");
            }
            if (matchingCorporates.size() > 1) {
                // more than one,  warn
                if (log.isInfoEnabled()) {
                    log.info("      "
                            + matchingCorporates.size()
                            + " multiple matches found.");
                }
            }
        }

        return corporateFound;
    }

    private void countUnmatchedRole(String role) {

        if ("composer".equals(role)) {
            this.count.incrementUnmatchedComposers();
        } else if ("creator".equals(role)) {
            this.count.incrementUnmatchedCreators();
        } else if ("realizer".equals(role)) {
            this.count.incrementUnmatchedRealizers();
        } else if ("producer".equals(role)) {
            this.count.incrementUnmatchedProducers();
        } else {
            log.warn("*** unknown unmatched role: " + role);
        }
    }
}
