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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.AuthorityHandler;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkDate;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkKey;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkPerformanceMedium;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkSubjectHeading;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkTitle;
import java.util.List;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *  Mapping a Work from a work field MarcDataField
 * and the including MarcRecord.
 * @author pbmcelwa
 */
public class WorkMapper {

    private static Logger log = Logger.getLogger(WorkMapper.class);

    private MarcDataField marcBibDataField;

    private MarcRecord marcBibRecord;

    private MarcRecord marcAuthRecord;

    private MarcDataField titleDataField;

    /**
     *  Create Mapper for Work attributes.
     * Work mapping uses both the authority record (if available)
     * and the bibliographic record.  Hence, the check for authority
     * record availability is performed inside the WorkMapper.
     * @param workField the MARC Bibliographic Field for a Work.
     * @param sourceRecord the MARC Bibliographic Record of the Work Field.
     */
    public WorkMapper(final MarcDataField workField,
                      final MarcRecord sourceRecord) {

        this.marcBibDataField = workField;
        this.marcBibRecord = sourceRecord;
    }

    /*
     *
     * ----------- utilitly services code
     *
     */
    /**
     *  Get the group for this WorkMapper marcBibRecord.
     *
     * @return String group tag for the marcBibRecord.
     */
    public final String getGroup() {
        return this.marcBibRecord.getGroup();
    }

    /**
     *  Get the uniform title string from a MARC Work bibliographic field.
     *
     * Note: used by AuthorityHandler, SingleFRBRizationRecord
     *
     * @return String value of the uniform title.
     */
    public String getUniformTitleString() {
        String uniformTitle = null;

        String fieldTag = this.marcBibDataField.getTag();

        if (fieldTag.equals("240")
                || fieldTag.equals("130")
                || fieldTag.equals("730")) {
            uniformTitle =
                    this.marcBibDataField.concatSubfields("amnr".toCharArray());

        } else if (fieldTag.equals("700")
                || fieldTag.equals("710")
                || fieldTag.equals("711")) {
            uniformTitle =
                    this.marcBibDataField.concatSubfields("tmnr".toCharArray());

        } // otherwise return null

        /*
         * note: 245 and 745 title fields are not "uniform" titles
         */

        return uniformTitle;

    }

    /**
     * Return simple title from |t subfield.
     * Used by AuthorityHandler for name of authority file
     * for works with no composer field.
     * @return String title from |t subfield.
     */
    public String getSimpleTitleString() {

        return this.marcBibDataField.getValue('t');
    }

    public List<WorkSubjectHeading> getSubjectHeadings(WorkJpa work) {
        List<WorkSubjectHeading> wsh = new ArrayList<WorkSubjectHeading>();

        String[] tempNoteFields = {"600", "610", "611", "630", "648",
                                   "650", "651", "653", "654", "655", "656",
                                   "657",
                                   "658", "662", "690", "691", "692", "693",
                                   "694",
                                   "695", "696", "697", "698", "699"};
        List<MarcDataField> noteFields = this.marcBibRecord.getDataFields(
                tempNoteFields);

        for (MarcDataField currentNote : noteFields) {
            String noteValue = currentNote.concatAllBut("23468");

            if (!noteValue.equals("")) {
                WorkSubjectHeading sh = new WorkSubjectHeading(work, noteValue);
                wsh.add(sh);
            }
        }
        return wsh;
    }

    public List<WorkPerformanceMedium> getPerformanceMediums(WorkJpa work) {
        ArrayList<WorkPerformanceMedium> performanceMediums =
                new ArrayList<WorkPerformanceMedium>();

        if (!marcBibDataField.hasSubfields("m".toCharArray())) {
            return performanceMediums;
        }

        String valueFromMarc = marcBibDataField.concatSubfields(
                "m".toCharArray());

        String[] listValueFromMarc = valueFromMarc.split(",");

        for (String currentInst : listValueFromMarc) {

            currentInst = currentInst.split("\\(")[0];
            currentInst = currentInst.trim();
            WorkPerformanceMedium wpm = new WorkPerformanceMedium(work);
            wpm.setText(currentInst);
            performanceMediums.add(wpm);
        }

        return performanceMediums;
    }

    public List<WorkKey> getKeys(WorkJpa work) {
        List<WorkKey> keys = new ArrayList<WorkKey>();
        String keyValue = marcBibDataField.concatSubfields("r".toCharArray());

        if (keyValue != null && !keyValue.equals("")) {

            WorkKey wk = new WorkKey(work, keyValue.trim());
            keys.add(wk);
        }

        return keys;
    }

    public boolean composerIsPerson() {
        boolean isPerson = false;

        if (this.marcBibDataField.getTag().equals("240")
                || this.marcBibDataField.getTag().equals("245")
                || this.marcBibDataField.getTag().equals("740")) {

            String[] personFields = {"100"};
            String[] corporateBodyFields = {"110", "111"};

            if (this.marcBibRecord.getDataFields(personFields) != null
                    && this.marcBibRecord.getDataFields(personFields).size() > 0) {
                // Person composer
                isPerson = true;

            } else if (this.marcBibRecord.getDataFields(corporateBodyFields)
                    != null
                    && this.marcBibRecord.getDataFields(corporateBodyFields).
                    size() > 0) {
                // CorporateBody composer
                isPerson = false;
            }
        } else if (this.marcBibDataField.getTag().equals("700")) {
            // Person composer
            isPerson = true;
        }

        return isPerson;
    }

    public String getComposerAuthIdent() {

        String authIdent = "";

        if (this.marcBibDataField.getTag().equals("240")
                || this.marcBibDataField.getTag().equals("245")
                || this.marcBibDataField.getTag().equals("740")) {

            String[] personFields = {"100"};
            String[] corporateBodyFields = {"110", "111"};

            if (this.marcBibRecord.getDataFields(personFields) != null
                    && this.marcBibRecord.getDataFields(personFields).size() > 0) {
                // Person composer
                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(personFields).get(0);
                authIdent = PersonMapper.getAuthIdent(contribDataField);

            } else if (this.marcBibRecord.getDataFields(corporateBodyFields)
                    != null
                    && this.marcBibRecord.getDataFields(corporateBodyFields).
                    size() > 0) {
                // CorporateBody composer
                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(corporateBodyFields).
                        get(0);
                authIdent = CorporateBodyMapper.getAuthIdent(
                        contribDataField);

            }
        } else if (this.marcBibDataField.getTag().equals("700")) {
            // Person composer
            authIdent = PersonMapper.getAuthIdent(this.marcBibDataField);
        }

        return authIdent;
    }

    public String getComposerNormalName() {

        String normalName = "";

        if (this.marcBibDataField.getTag().equals("240")
                || this.marcBibDataField.getTag().equals("245")
                || this.marcBibDataField.getTag().equals("740")) {

            String[] personFields = {"100"};
            String[] corporateBodyFields = {"110", "111"};

            if (this.marcBibRecord.getDataFields(personFields) != null
                    && this.marcBibRecord.getDataFields(personFields).size() > 0) {
                // Person composer
                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(personFields).get(0);
                normalName = PersonMapper.getNormalName(contribDataField);

            } else if (this.marcBibRecord.getDataFields(corporateBodyFields)
                    != null
                    && this.marcBibRecord.getDataFields(corporateBodyFields).
                    size() > 0) {
                // CorporateBody composer
                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(corporateBodyFields).
                        get(0);
                normalName = CorporateBodyMapper.getNormalName(
                        contribDataField);

            }
        } else if (this.marcBibDataField.getTag().equals("700")) {
            // Person composer
            normalName = PersonMapper.getNormalName(this.marcBibDataField);
        }

        return normalName;
    }

    /*
     * Note: used by SingleFRBRizationRecord
     */
    public String getComposer() {

        String contributorName = "";

        if (this.marcBibDataField.getTag().equals("240")
                || this.marcBibDataField.getTag().equals("245")
                || this.marcBibDataField.getTag().equals("740")) {

            String[] personFields = {"100"};
            String[] corporateBodyFields = {"110", "111"};

            if (this.marcBibRecord.getDataFields(personFields) != null
                    && this.marcBibRecord.getDataFields(personFields).size() > 0) {

                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(personFields).get(0);
                contributorName = PersonMapper.getPersistNameString(
                        contribDataField);

            } else if (this.marcBibRecord.getDataFields(corporateBodyFields)
                    != null
                    && this.marcBibRecord.getDataFields(corporateBodyFields).
                    size() > 0) {

                MarcDataField contribDataField =
                        this.marcBibRecord.getDataFields(corporateBodyFields).
                        get(0);
                contributorName = CorporateBodyMapper.getPersistNameString(
                        contribDataField);
            }
        } else if (this.marcBibDataField.getTag().equals("700")) {
            contributorName =
                    PersonMapper.getPersistNameString(marcBibDataField);
        }

        return contributorName;
    }

    /*
     * Note: used by AuthorityHandler
     */
    public MarcDataField getComposerField() {
        if (this.marcBibDataField.getTag().equals("240")
                || this.marcBibDataField.getTag().equals("245")
                || this.marcBibDataField.getTag().equals("740")) {

            String[] personFields = {"100"};
            String[] corporateBodyFields = {"110", "111"};

            if (this.marcBibRecord.getDataFields(personFields) != null
                    && this.marcBibRecord.getDataFields(personFields).size() > 0) {

                return this.marcBibRecord.getDataFields(personFields).get(0);

            } else if (this.marcBibRecord.getDataFields(corporateBodyFields)
                    != null
                    && this.marcBibRecord.getDataFields(corporateBodyFields).
                    size() > 0) {

                return this.marcBibRecord.getDataFields(corporateBodyFields).get(
                        0);

            }
        } else if (this.marcBibDataField.getTag().equals("700")) {

            return this.marcBibDataField;
        }

        return null; //possible for work field not to have composer
    }

    public List<WorkDate> getDates(WorkJpa work) {

        List<WorkDate> dates = new ArrayList<WorkDate>();

        if (this.marcBibRecord.hasField("045")) {
            MarcDataField field045 = this.marcBibRecord.getDataField("045");
            String dateStringValue = field045.concatSubfields("b".toCharArray());
            WorkDate workDate = new WorkDate(work, dateStringValue);
            dates.add(workDate);
        }

        return dates;
    }

    public List<WorkTitle> getVariantTitles() { //For authority records only

        List<WorkTitle> returnTitles = new ArrayList<WorkTitle>();
        String[] workFields = {"400", "410", "411"};
        List<MarcDataField> variantFields =
                this.marcBibRecord.getDataFields(workFields);

        for (MarcDataField currentField : variantFields) {

            String title = currentField.concatSubfields("tmnr".toCharArray());
            WorkTitle currentTitle = new WorkTitle();
            currentTitle.setType("variant");
            currentTitle.setText(title.trim());
            returnTitles.add(currentTitle);
        }

        return returnTitles;
    }

    /*
     * 
     * ------------- primary mapping code
     * 
     * 
     */
    /**
     *  Map a Work in the context of the WorkMapper instance.
     *
     * @param work WorkJpa work to map.
     */
    public final MarcRecord mapWork(final WorkJpa work) {

        /*
         * check for existence of authority cache record
         * will be null if not found
         */
        final AuthorityHandler authHandler = new AuthorityHandler();
        this.marcAuthRecord = authHandler.getWorkRecordFromAuthority(this);

        if (log.isInfoEnabled()) {
            log.info("  -- bib field: "
                    + this.marcBibDataField.toString());
        }

        if (this.marcAuthRecord != null) {
            if (log.isInfoEnabled()) {
                log.info("  -- matching cached authority record: \n"
                        + this.marcAuthRecord.toString());
            }
        }

        /*
         * set up the mapper implementation
         */
        final WorkMap workMap = new WorkMap(this.marcBibRecord,
                                            this.marcAuthRecord,
                                            this.marcBibDataField,
                                            this);

        /*
         * map the work attributes into the provided Work
         * from the MARC bibliographic record
         *  and, if it exists, the cached MARC authority record
         */

        // -- uniformTitle
        //  String uniformTitle
        // this is done in the title mapping

        // -- titleOfTheWork
        //  List<WorkTitle> titles
        workMap.mapTitles(work);

        // -- authIdent
        //   use the uniformTitle determined in mapTitles()
        //   so must follow
        workMap.mapAuthIdent(work);


        // -- formOfWork
        //  List<WorkForm> forms
        workMap.mapForms(work);

        // -- dateOfTheWork
        //  List<WorkDate> dates
        workMap.mapDates(work);

        // -- otherDistinguishingCharacteristic
        //  List<WorkCharacteristic> characteristics
        // not mapped

        // -- intendedAudience
        //  List<WorkAudience> audiences
        workMap.mapAudiences(work);

        // -- contextForTheWork
        //  List<WorkContext> contexts
        // not mapped

        // -- mediumOfPerformance
        //  List<WorkPerformanceMedium> performanceMediums
        workMap.mapPerformanceMediums(work);

        // -- numericDesignation
        //  List<WorkDesignation> designations
        workMap.mapDesignations(work);

        // -- key
        //  List<WorkKey> keys
        workMap.mapKeys(work);

        // -- subjectOfTheWork
        //  List<WorkSubjectHeading> subjectHeadings
        workMap.mapSubjectHeadings(work);

        // -- placeOfOriginOfTheWork
        //  List<WorkOriginPlace> originPlaces
        // not mapped

        // -- history
        //  List<WorkHistory> histories
        // not mapped

        // -- note
        //  List<WorkNote> notes
        workMap.mapNotes(work);

        // -- Language
        //  List<WorkLanguage> languages

        // -- placeOfComposition
        //  List<WorkCompositionPlace> compositionPlaces
        // not mapped

        if (log.isInfoEnabled()) {
            log.info("  -- work mapped.");
        }

        return this.marcAuthRecord;
    }
}
