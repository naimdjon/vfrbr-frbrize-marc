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
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcCollection;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Properties;
import org.apache.log4j.Logger;

import org.yaz4j.Connection;
import org.yaz4j.ResultSet;


public class AuthorityHandler {

    private static final Logger log = Logger.getLogger(AuthorityHandler.class);

    private String CACHE_PATH;

    private String CORP_CACHE_PATH;

    private String PEOPLE_CACHE_PATH;

    private String WORK_CACHE_PATH;

    private String AUTHORTY_HOST;
    private int AUTHORITY_PORT;
    private String AUTHORITY_DATABASE;
    private String AUTHORITY_USERNAME;
    private String AUTHORITY_PASSWORD;

    //form for attribute arrays
    //USE - RELATION - POSITION - STRUCTURE - TRUNCATION - COMPLETENESS
    //see http://www.loc.gov/z3950/agency/bib1.html for descriptions
    private final String WORK_ATTRIB = "@attr 1=4 @attr 2=104 @attr 3=1 @attr 4=1 @attr 5=100 @attr 6=3 ";
    private final String PERSON_ATTRIB = "@attr 1=1 @attr 2=104 @attr 3=1 @attr 4=1 @attr 5=100 @attr 6=3 ";
    private final String CORP_ATTRIB = "@attr 1=2 @attr 2=104 @attr 3=1 @attr 4=1 @attr 5=100 @attr 6=3 ";

    /**
     * Instantiate an AuthorityHandler,
     * also loading the authority handler properties.
     */
    public AuthorityHandler() {
        final ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
        final InputStream inSteam =
                loader.getResourceAsStream("authCache.properties");
        if (inSteam == null) {
            log.error("==*!!*== Can't load authCache.properties.");
        } else {
            try {
                final Properties authCacheProps = new Properties();
                authCacheProps.load(inSteam);
                // and set the path fields
                CACHE_PATH =
                        authCacheProps.getProperty("auth_cache_root");
                CORP_CACHE_PATH =
                        CACHE_PATH + authCacheProps.getProperty("auth_cache_path_corp");
                PEOPLE_CACHE_PATH =
                        CACHE_PATH + authCacheProps.getProperty("auth_cache_path_people");
                WORK_CACHE_PATH =
                        CACHE_PATH + authCacheProps.getProperty("auth_cache_path_work");

                AUTHORTY_HOST = authCacheProps.getProperty("authority_host");
                AUTHORITY_PORT = Integer.getInteger(authCacheProps.getProperty("authority_port"));
                AUTHORITY_DATABASE = authCacheProps.getProperty("authority_database");
                AUTHORITY_USERNAME = authCacheProps.getProperty("authority_username");
                AUTHORITY_PASSWORD = authCacheProps.getProperty("authority_password");

            } catch (IOException ex) {
                log.error("==*!!*== load failed for authCache.properties.");
            }
        }
    }

    /**
     *  Get a cached person authority file matching the
     * MarcDataField personField, or return null.
     *
     * @param personField the MarcDataField for the person.
     * @return a cached authority MarcRecord, or null.
     */
    public final MarcRecord getAuthorityPersonRecord(final MarcDataField personField) {

        final String nameFromMARC =
                personField.concatSubfields("abcdejq".toCharArray());

        MarcRecord selectedRecord = null;

        int recIndx = 0;

        try {
            MarcCollection peopleCache = this.getAuthRecords(nameFromMARC, PEOPLE_CACHE_PATH, PERSON_ATTRIB);
            if(peopleCache == null) {
                return null;
            }
            int numberOfCanidates = 0;
            while (peopleCache.hasNext()) {
                recIndx++;
                final MarcRecord marcRec = peopleCache.next();
                if (marcRec.hasField("100")) {
                    final MarcDataField field100 = marcRec.getDataField("100");
                    if (!field100.hasSubfields("t".toCharArray())) {

                        selectedRecord = marcRec;
                        numberOfCanidates++;
                    }
                }
            }

            if (numberOfCanidates == 1) {
                return selectedRecord;
            } else if (numberOfCanidates > 1) {
                peopleCache = this.getAuthRecords(nameFromMARC, PEOPLE_CACHE_PATH, PERSON_ATTRIB);
                while (peopleCache.hasNext()) {
                    final MarcRecord marcRec = peopleCache.next();
                    if (marcRec.hasField("100")) {
                        final MarcDataField field100 =
                                marcRec.getDataField("100");
                        if (!field100.hasSubfields("t".toCharArray())) {
                            final String fieldValue100 =
                                    field100.concatSubfields(
                                    "abcdejq".toCharArray());
                            // FIXME String.equals probably doesn't handle extended utf
                            if (nameFromMARC.equals(fieldValue100)) {
                                return marcRec;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            log.error("\n-- exception processing MarcCollection on fileName for \""
                    + nameFromMARC + "\"\n"
                    + "- at authRecIndx: " + recIndx);
            log.error("\n---\n" + ex.getMessage());
            log.error("\n--- stacktrace:\n", ex);
            log.error("\n---\n");
            log.error("  returning null and continuing...");
            return null;
        }
    }

    /**
     * Get a matching cached authority corporate MarcRecord
     * or return null.
     * @param corpBodyField the corporate MarcDataField to match.
     * @return a matching authority MarcRecord or null.
     */
    public final MarcRecord getAuthorityCorporateBodyRecord(
            final MarcDataField corpBodyField) {

        final String nameFromMARC =
                corpBodyField.concatSubfields("abcdenq".toCharArray());
        final String[] corpFields = {"110", "111"};
        MarcRecord selectedRecord = null;

        try {
            MarcCollection corpCache = this.getAuthRecords(nameFromMARC, CORP_CACHE_PATH, CORP_ATTRIB);
            if(corpCache == null) {
                return null;
            }
            int numberOfCanidates = 0;
            while (corpCache.hasNext()) {
                final MarcRecord marcRec = corpCache.next();
                final MarcDataField field11x =
                        marcRec.getDataFields(corpFields).get(0);
                if (!field11x.hasSubfields("t".toCharArray())) {
                    selectedRecord = marcRec;
                    numberOfCanidates++;
                }
            }
            if (numberOfCanidates == 1) {
                return selectedRecord;
            } else if (numberOfCanidates > 1) {
                corpCache = this.getAuthRecords(nameFromMARC, CORP_CACHE_PATH, CORP_ATTRIB);
                while (corpCache.hasNext()) {
                    final MarcRecord marcRec = corpCache.next();
                    final MarcDataField field11x =
                            marcRec.getDataFields(corpFields).get(0);
                    if (!field11x.hasSubfields("t".toCharArray())) {
                        final String fieldValue11x =
                                field11x.concatSubfields("abcdenq".toCharArray());
                        if (nameFromMARC.equals(fieldValue11x)) {
                            return marcRec;
                        }
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     *  Get a matching cached authority Work MarcRecord
     * or return null.
     * @param workMapper the WorkMapper holding the Work MarcDataField to match.
     * @return the matching cached authority MarcRecord or null.
     */
    public final MarcRecord getWorkRecordFromAuthority(
            final WorkMapper workMapper) {

        final MarcDataField composerField = workMapper.getComposerField();
        String name = "";
        String path;
        String attribSet;
        if (null != composerField) {

            if ((composerField.getTag().equals("100"))
                    || (composerField.getTag().equals("700"))) {

                name = composerField.concatSubfields("abcdejq".toCharArray());
                path = PEOPLE_CACHE_PATH;
                attribSet = PERSON_ATTRIB;

            } else {

                name = composerField.concatSubfields("abcdenq".toCharArray());
                path = CORP_CACHE_PATH;
                attribSet = CORP_ATTRIB;
            }
        } else {
            // no composer field, use work title
            // as done in auth cache file creation (|t subfield)
            name = workMapper.getSimpleTitleString();
            path = WORK_CACHE_PATH;
            attribSet = WORK_ATTRIB;
        }

        try {
            final MarcCollection marcCache = this.getAuthRecords(name, path, attribSet);
            while (marcCache.hasNext()) {
                final MarcRecord marcRec = marcCache.next();
                final String[] headingFields = {"100", "110", "111"};

                if (marcRec.hasField(headingFields)) {
                    final MarcDataField mdf =
                            marcRec.getDataFields(headingFields).get(0);
                    if (mdf.hasSubfields("t".toCharArray())
                            && !mdf.hasSubfields("kpo".toCharArray())) {
                        if (normalize(
                                workMapper.getUniformTitleString(),
                                true).equals(
                                normalize(mdf.concatSubfields(
                                "tmnr".toCharArray()), true))) {
                            return marcRec;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Error reading authority file", ex);
            return null;
        }
        return null;
    }

    /**
     * Normalization for cached file name matching.
     * @param unNormal String to normalize.
     * @param lowerCase whether to lowercase the result.
     * @return normalized String.
     */
    public static String normalize(final String unNormal,
                             final boolean lowerCase) {
        String normaled = unNormal;
        if (normaled == null) {
            // always return something even from nothing
            normaled = "";
        } else {
            // something to normalize
            normaled = normaled.trim();
            normaled = normaled.replaceAll(" +", " ");
            normaled = Normalizer.normalize(normaled, Form.NFD);
            normaled = normaled.replaceAll("[^\\p{ASCII}]", "");

            if (lowerCase) {
                normaled = normaled.toLowerCase();
            }
        }

        return normaled;
    }

    private String toFileName(String in, String path) {
        String fileName = normalize(in, true);
        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll("[^a-zA-Z0-9\\-_]", "");
        fileName = path + fileName + ".mrc";
        fileName = fileName.replace("-.mrc", ".mrc");
        return fileName;
    }

    private MarcCollection getAuthRecords(String searchString, String cache, String attribSet) {
        log.info("processing: " + searchString);
        String fileName = toFileName(searchString, cache);
        File f = new File(fileName);
        if(f.exists()) {
            log.info("auth file exists for " + searchString);
            try {
                return new MarcCollection(fileName);
            } catch(Exception ex) {
                log.error(ex);
                return null;
            }
        } else {
            Connection con = new Connection(AUTHORTY_HOST, AUTHORITY_PORT);
            con.setUsername(AUTHORITY_USERNAME);
            con.setPassword(AUTHORITY_PASSWORD);
            con.setDatabaseName(AUTHORITY_DATABASE);

            try {
                con.connect();

                searchString = searchString.replaceAll("\"", "\\\\\"");

                String query = attribSet + "\"" + searchString + "\"";

                log.info("searching: " + query);

                ResultSet set = con.search(query, Connection.QueryType.PrefixQuery);
                log.info("Found:" + set.getHitCount());
                if(set.getHitCount() > 0) {
                    FileOutputStream fos = new FileOutputStream(f);
                    for(int i = 0; i < set.getHitCount(); i++) {
                        fos.write(set.getRecord(i).getContent());
                    }
                    fos.close();
                } else {
                    f.createNewFile(); //create blank file
                }
                Thread.sleep(250);  //so we don't overwhelm the server

                con.close();
                return new MarcCollection(fileName);
            } catch (Exception ex) {
                log.error(ex);
                return null;
            }
        }
    }
}
