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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcCompositionFormCodes;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcControlField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcLanguageCodes;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.NormalIdent;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkAudience;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkDate;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkDesignation;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkForm;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkKey;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkLanguage;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkNote;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkPerformanceMedium;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkSubjectHeading;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkTitle;
import java.util.List;
import java.util.Properties;

/**
 *  Implementation mapping of Work
 * from bibliographic MarcDataField work field and possibly also
 * an authority MarcRecord.
 *
 */
public class WorkMap {

    private final MarcDataField marcBibDataField;

    private final MarcRecord marcBibRecord;

    private final MarcRecord marcAuthRecord;

    private final WorkMapper ourWorkMapper;

    private MarcDataField titleDataField;

    /**
     *  Create a WorkMap instance and initialize the mapping context.
     *
     * @param sourceMarcRecord MarcRecord source of the work field.
     * @param authorityMarcRecord authority MarcRecord for the work.
     * @param workMarcDataField work field MarcDataField from the
     * source MarcRecord.
     */
    public WorkMap(final MarcRecord sourceMarcRecord,
                   final MarcRecord authorityMarcRecord,
                   final MarcDataField workMarcDataField,
                   final WorkMapper invokingWorkMapper) {


        this.marcBibRecord = sourceMarcRecord;

        this.marcAuthRecord = authorityMarcRecord;

        this.marcBibDataField = workMarcDataField;

        this.ourWorkMapper = invokingWorkMapper;
    }

    /**
     *  Map titleOfTheWork.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapTitles(final WorkJpa work) {
        // if a matching record from the authority cache
        if (this.marcAuthRecord != null) {
            // -- map from authority record
            /*
             * get the uniform title
             *  save the data field used for mapForms()
             */
            final String[] field1xxTags = {"100", "110", "111"};

            if (this.marcAuthRecord.hasField(field1xxTags)) {
                // should be just one, but....
                final List<MarcDataField> field1xxList =
                        this.marcAuthRecord.getDataFields(field1xxTags);

                final WorkTitle title =
                        new WorkTitle(
                        work,
                        field1xxList.get(0).concatSubfields("tmnr".toCharArray()));
                title.setOffset(0);
                title.setType("uniform");
                title.setVocabulary("naf");

                work.setUniformTitle(title.getText());
                work.getTitles().add(title);

                // save the titleDataField
                this.titleDataField = field1xxList.get(0);

            } else if (this.marcAuthRecord.hasField("130")) {

                final MarcDataField field130 =
                        this.marcAuthRecord.getDataField("130");

                final WorkTitle title = new WorkTitle(
                        work,
                        field130.concatSubfields("amnr".toCharArray()));
                title.setOffset(0);
                title.setType("uniform");
                title.setVocabulary("naf");

                work.setUniformTitle(title.getText());
                work.getTitles().add(title);

                // save the titleDataField
                this.titleDataField = field130;
            }
            // otherwise no uniform title ?!!?
            /*
             * get the variant titles
             *  if uniformTitle didn't save a field, save this one
             */
            final String[] field4xxTags = {"400", "410", "411"};

            for (MarcDataField field4xx :
                    this.marcAuthRecord.getDataFields(field4xxTags)) {

                final WorkTitle title = new WorkTitle(
                        work,
                        field4xx.concatSubfields("tmnr".toCharArray()));
                title.setOffset(0);
                title.setType("variant");

                work.getTitles().add(title);

                // if no authority title found,
                // save the first variant
                if (this.titleDataField == null) {
                    this.titleDataField = field4xx;
                }
            }
            for (MarcDataField field430 :
                    this.marcAuthRecord.getDataFields("430")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        field430.concatSubfields("amnr".toCharArray()));
                title.setOffset(0);
                title.setType("variant");

                work.getTitles().add(title);

                // if no title found yet,
                // save this one
                if (this.titleDataField == null) {
                    this.titleDataField = field430;
                }
            }
        } else {
            // no matching authority record
            // -- map from bibliographic record field

            final MarcDataField bibDataField = this.marcBibDataField;
            final String bibDataFieldTag = bibDataField.getTag();

            if (bibDataFieldTag.equals("240")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        bibDataField.concatSubfields("amnr".toCharArray()));
                try {
                    title.setOffset(
                            Integer.parseInt(bibDataField.get2ndIndicator()));
                } catch (NumberFormatException ex) {
                    // should this be logged?
                }
                title.setType("uniform");
                title.setVocabulary("naf");
                work.setUniformTitle(title.getText());
                work.getTitles().add(title);

                // save title data field
                this.titleDataField = bibDataField;

            } else if (bibDataFieldTag.equals("130")
                    || bibDataFieldTag.equals("730")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        bibDataField.concatSubfields("amnr".toCharArray()));
                try {
                    title.setOffset(
                            Integer.parseInt(bibDataField.get1stIndicator()));
                } catch (NumberFormatException ex) {
                    // should this be logged?
                }
                title.setType("uniform");
                title.setVocabulary("naf");
                work.setUniformTitle(title.getText());
                work.getTitles().add(title);

                // save title data field
                this.titleDataField = bibDataField;

            } else if (bibDataFieldTag.equals("700")
                    || bibDataFieldTag.equals("710")
                    || bibDataFieldTag.equals("711")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        bibDataField.concatSubfields("tmnr".toCharArray()));
                title.setType("uniform");
                title.setVocabulary("naf");
                work.setUniformTitle(title.getText());
                work.getTitles().add(title);

                // save title data field
                this.titleDataField = bibDataField;

            } else if (bibDataFieldTag.equals("740")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        bibDataField.concatSubfields("an".toCharArray()));
                try {
                    title.setOffset(
                            Integer.parseInt(bibDataField.get1stIndicator()));
                } catch (NumberFormatException ex) {
                    // should this be logged ??
                }

                work.getTitles().add(title);

                // save title data field
                this.titleDataField = bibDataField;

            } else if (bibDataFieldTag.equals("245")) {

                final WorkTitle title = new WorkTitle(
                        work,
                        bibDataField.getValue('a'));
                try {
                    title.setOffset(
                            Integer.parseInt(bibDataField.get2ndIndicator()));
                } catch (NumberFormatException ex) {
                    // should this be logged ??
                }
                title.setType("titleproper");
                title.setVocabulary("aacr2");

                work.getTitles().add(title);

                // save title data field
                this.titleDataField = bibDataField;

            }
            // otherwise ??
        }
    }

    /**
     * Map authIdent.
     * Use uniformTitle if there is one.
     * Otherwise use first title, if there is one.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapAuthIdent(final WorkJpa work) {

        // if there is a uniformTitle
        if (null != work.getUniformTitle()) {
            work.setAuthIdent(
                    NormalIdent.getAuthIdent(work.getUniformTitle())
                    + "::"
                    + this.ourWorkMapper.getComposerAuthIdent());
        } //
        // otherwise use workTitle
        else {
            // if there is one
            if (work.getTitles().size() > 0) {
                work.setAuthIdent(
                        NormalIdent.getAuthIdent(work.getTitles().get(0).getText())
                        + "::"
                        + this.ourWorkMapper.getComposerAuthIdent());
            } //
            // otherwise we've run out of title options
            else {
                work.setAuthIdent(
                        "::"
                        + this.ourWorkMapper.getComposerAuthIdent());
            }
        }

    }

    /**
     *  Map formOfWork.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapForms(final WorkJpa work) {

        if (!this.titleDataField.hasSubfields("o".toCharArray())) {
            // no |o arr. in title field

            final Properties compFormCodeProps =
                    new MarcCompositionFormCodes().getCompositionFormCodeProperties();

            if (this.marcBibRecord.hasField("008")) {
                final String compFormCode =
                        this.marcBibRecord.getControlField("008").
                        getRange(18, 19);
                if (!compFormCode.equals("mu")) {
                    // use form from 008

                    final WorkForm workForm = new WorkForm(
                            work,
                            compFormCodeProps.getProperty(compFormCode));
                    workForm.setVocabulary("marcformofcomposition");

                    work.getForms().add(workForm);
                }
            } else {
                // no 008 or 008 comp is "mu", use 047 (R) |a (R) forms
                for (MarcDataField field047 :
                        this.marcBibRecord.getDataFields("047")) {
                    for (String compFormCode :
                            field047.getValueList('a')) {

                        final WorkForm workForm = new WorkForm(
                                work,
                                compFormCodeProps.getProperty(compFormCode));
                        workForm.setVocabulary("marcformofcomposition");

                        work.getForms().add(workForm);
                    }
                }
            }

        } else {
            // has |o arr. in title field
            if (this.titleDataField.getTag().equals("240")
                    || this.titleDataField.getTag().equals("130")
                    || this.titleDataField.getTag().equals("730")) {
                // AND
                if (this.titleDataField.hasSubfields("mnr".toCharArray())) {

                    final WorkForm workForm = new WorkForm(
                            work,
                            this.titleDataField.getValue('a'));
                    workForm.setVocabulary("aacr2");

                    work.getForms().add(workForm);
                }

            } else if (this.titleDataField.getTag().equals("700")
                    || this.titleDataField.getTag().equals("710")
                    || this.titleDataField.getTag().equals("711")) {
                // AND
                if (this.titleDataField.hasSubfields("mnr".toCharArray())) {

                    final WorkForm workForm = new WorkForm(
                            work,
                            this.titleDataField.getValue('t'));
                    workForm.setVocabulary("aacr2");

                    work.getForms().add(workForm);
                }
            }
        }
    }

    /**
     * Map dateOfTheWork.
     *
     * @param work the WorkJpa Work for attribute mapping.
     */
    protected final void mapDates(final WorkJpa work) {

        final String[] dateTags = {
            "240", "130",
            "700", "710", "711", "720", "730", "740"};

        final String workIdentTag = this.titleDataField.getTag();

        boolean inDateTags = false;

        for (String dateTag : dateTags) {
            if (workIdentTag.equals(dateTag)) {
                inDateTags = true;
            }
        }

        if (inDateTags) {
            // 3.1. work is identified from 240/130/7xx
            if (this.titleDataField.hasSubfields("f".toCharArray())) {
                // |f (NR)
                final String subfieldF = this.titleDataField.getValue('f');
                if (subfieldF.matches("^\\(\\d{4}\\)")) {
                    // 3.1.1.1. "(xxxx)"
                    final WorkDate date = new WorkDate(work, subfieldF);
                    // TODO validate |f mapped to date text value
                    date.setType("single");
                    date.setNormal(subfieldF.substring(1, 5));

                    work.getDates().add(date);

                } else if (subfieldF.matches("^\\(\\d{4}\\-\\d{2}+\\)")) {
                    // 3.1.1.2. "(xxxx-xx)"    (1234-56)
                    final WorkDate date = new WorkDate(work, subfieldF);
                    date.setType("range");
                    date.setNormal(
                            subfieldF.substring(1, 5) // 1234
                            + "/"
                            + subfieldF.substring(1, 3) // 12
                            + subfieldF.substring(6, 8));    // 56

                    work.getDates().add(date);

                } else if (subfieldF.matches("^\\(\\d{4}\\-\\d{4}+\\)")) {
                    // 3.1.1.3. "(xxxx-xxxx)"
                    final WorkDate date = new WorkDate(work, subfieldF);
                    date.setType("range");
                    date.setNormal(
                            subfieldF.substring(1, 5)
                            + "/"
                            + subfieldF.substring(6, 10));

                    work.getDates().add(date);

                }
                // else no parenthetical dates, do same as no dates
                // TODO validate this logic point
                mapDate045(work);

            } else {
                // 3.1.2. no specified date pattern in |f, use 045 |b
                mapDate045(work);
            }
        } else if (workIdentTag.equals("245")) {
            // 3.2 
            mapDate045(work);
        }
    }

    /**
     * Helper mapping of field045 date source.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    private void mapDate045(final WorkJpa work) {
        // 3.1.2. use 045 (NR) |b (R)
        if (this.marcBibRecord.hasField("045")) {
            final MarcDataField field045 =
                    this.marcBibRecord.getDataField("045");
            final String indicator1 = field045.get1stIndicator();
            if ("0".equals(indicator1)) {
                // use first |b following "d"
                final String subfieldB = field045.getValue('b');
                if (subfieldB != null && (subfieldB.indexOf('d') >= 0)) {
                    final int posD = subfieldB.indexOf('d');
                    if ((subfieldB.length() - posD - 1) >= 4) {
                        // at least 4 characters follow "d"
                        final WorkDate date = new WorkDate(
                                work,
                                subfieldB.substring(posD + 1, posD + 5));
                        date.setType("single");
                        date.setNormal(date.getText());

                        work.getDates().add(date);
                    }
                    // else not enough date characters for specified mapping
                }
            } else if ("1".equals(indicator1)) {
                // use all |b following "d"
                for (String subfieldB : field045.getValueList('b')) {
                    final int posD = subfieldB.indexOf('d');
                    if ((posD >= 0) && ((subfieldB.length() - posD - 1) >= 4)) {
                        // "d" found and at least 4 characters follow
                        final WorkDate date = new WorkDate(
                                work,
                                subfieldB.substring(posD + 1, posD + 5));
                        date.setType("single");
                        date.setNormal(date.getText());

                        work.getDates().add(date);
                    }
                    // else not enough date characters for specified mapping
                }
            } else if ("2".equals(indicator1)) {
                // use first two |b as a range pair
                final List<String> subfieldBList = field045.getValueList('b');
                if (subfieldBList.size() >= 2) { // at least two dates found
                    // first date field
                    final String subfieldB1 = subfieldBList.get(0);
                    final int posD1 = subfieldB1.indexOf('d');
                    // second date field
                    final String subfieldB2 = subfieldBList.get(1);
                    final int posD2 = subfieldB2.indexOf('d');
                    // are date characters there
                    if (((posD1 >= 0)
                         && ((subfieldB1.length() - posD1 - 1) >= 4))
                            && ((posD2 >= 0)
                                && ((subfieldB2.length() - posD2 - 1) >= 4))) {
                        // "d" found followed by at least 4 characters
                        //  in both subfields

                        final String dateStr1 =
                                subfieldB1.substring(posD1 + 1, posD1 + 5);
                        final String dateStr2 =
                                subfieldB2.substring(posD2 + 1, posD2 + 5);

                        final WorkDate date = new WorkDate(work);
                        date.setText(dateStr1 + "-" + dateStr2);
                        date.setNormal(dateStr1 + "/" + dateStr2);
                        date.setType("range");

                        work.getDates().add(date);
                    }
                    // else not enough date characters in a subfield
                }
                // else not enough date subfields for specified mapping,
                // do nothing
            }
            // else state not specified in mapping, no action
        }
        // else no 045 field, do nothing
    }

    /**
     *  Map intendedAudience.
     *
     * @param work WorkJpa work for attribute mapping.
     */
    protected final void mapAudiences(final WorkJpa work) {

        if (this.marcBibRecord.hasField("008")) {

            final MarcControlField ctlField008 =
                    this.marcBibRecord.getControlField("008");
            final String audienceCode = ctlField008.getRange(22, 22);
            String audienceText = null;

            /*
            # - Unknown or unspecified
            a - Preschool
            b - Primary
            c - Pre-adolescent
            d - Adolescent
            e - Adult
            f - Specialized
            g - General
            j - Juvenile
            | - No attempt to code
             */

            if (null != audienceCode) {
                // a code
                if ("#".equals(audienceCode)) {
                    audienceText = "Uknown or unspecified";
                } else if ("a".equals(audienceCode)) {
                    audienceText = "Preschool";
                } else if ("b".equals(audienceCode)) {
                    audienceText = "Primary";
                } else if ("c".equals(audienceCode)) {
                    audienceText = "Pre-adolescent";
                } else if ("d".equals(audienceCode)) {
                    audienceText = "Adolescent";
                } else if ("e".equals(audienceCode)) {
                    audienceText = "Adult";
                } else if ("f".equals(audienceCode)) {
                    audienceText = "Specialized";
                } else if ("g".equals(audienceCode)) {
                    audienceText = "General";
                } else if ("j".equals(audienceCode)) {
                    audienceText = "Juvenile";
                } else if ("|".equals(audienceCode)) {
                    audienceText = "No attempt to code";
                }

                // was it recognized
                if (null != audienceText) {

                    final WorkAudience audience = new WorkAudience(work, audienceText);

                    work.getAudiences().add(audience);
                }
                // else don't create one
            }
        }
        // else no 008 control field ??
    }

    /**
     *  Map mediumOfPerformance.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapPerformanceMediums(final WorkJpa work) {

        if (this.marcAuthRecord != null) {
            // map from authority record
            final String[] headingFields = {
                // 1xx
                "100", "110", "111", "130", "148",
                "150", "151", "155", "180", "181", "182", "185"};
            final String[] seeFromFields = {
                // 4xx
                "400", "410", "411", "430", "448", "450", "451", "455",
                "480", "481", "482", "485"};
            boolean found1xxM = false;
            boolean found1xxO = false;
            if (this.marcAuthRecord.hasField(headingFields)) {
                // map from 1xx |m (R)
                for (MarcDataField field1xx :
                        this.marcAuthRecord.getDataFields(headingFields)) {
                    // flag finding of |m or |o for field 490 considerations
                    if (field1xx.hasSubfields("o".toCharArray())) {
                        found1xxO = true;
                    }
                    if (field1xx.hasSubfields("m".toCharArray())) {
                        found1xxM = true;
                    }
                    for (String subfieldM : field1xx.getValueList('m')) {

                        mapPerfMedSubfieldM(work, subfieldM);
                    }
                }
            } else if (!found1xxM && !found1xxO
                    && this.marcAuthRecord.hasField(seeFromFields)) {
                // map from 4xx|m if no 1xx|m or 1xx|o encountered

                // 4xx (R)
                for (MarcDataField field4xx :
                        this.marcAuthRecord.getDataFields(seeFromFields)) {
                    // |m (R)
                    for (String subfieldM : field4xx.getValueList('m')) {

                        mapPerfMedSubfieldM(work, subfieldM);
                    }
                }
            }

        } else {
            // map from bibliographic record

            // if the work comes from a
            final String[] mapBibRecs = {
                "130", "240", "700", "710", "711", "730"};
            boolean isMapBibRec = false;
            final String workTitleFieldTag = this.titleDataField.getTag();
            for (String mapBibRecTag : mapBibRecs) {
                if (workTitleFieldTag.equals(mapBibRecTag)) {
                    isMapBibRec = true;
                }
            }
            if (isMapBibRec) {

                // map subfield |m (R)
                for (String subfieldM :
                        this.titleDataField.getValueList('m')) {

                    mapPerfMedSubfieldM(work, subfieldM);
                }
            }
        }
    }

    /**
     *  Helper mapping of mediumOfPerformance
     * from subfield m.
     *
     * @param work the WorkJpa work for attribute mapping.
     * @param subfieldM subfield m.
     */
    private void mapPerfMedSubfieldM(final WorkJpa work,
                                     final String subfieldM) {

        // there may be multiple parts separated by commas,
        // although the last part may have a trailing comma

        // starting with the current position at the beginning
        int begSubstring = 0;
        // test for a comma seperating multiple entries
        int endSubstring = subfieldM.indexOf(',');
        if (endSubstring < 0) {  // if there is no comma
            // set comma position to the length of the entry
            endSubstring = subfieldM.length();
        }
        // now, consider the substring of the subfield
        // from the current position (inclusive)
        // to the comma position (exclusive)
        while (endSubstring > begSubstring) { // more string to consider
            // make entry from [begSubstring, endSubstring-1]
            final String entryString =
                    subfieldM.substring(begSubstring, endSubstring);
            int endTextual = entryString.length();
            // if there are trailing parentheses
            // ASSUMPTION: "(x)" always in trailing position, and only one


            // exclude "(x)" from the text entry

            final int quantBegPos = entryString.indexOf('(');
            final int quantEndPos = entryString.indexOf(')');
            int quant = -1;
            if ((quantBegPos >= 0)
                    && (quantEndPos >= 0)
                    && (quantEndPos > quantBegPos)) {
                // if both parentheses found as a pair
                // exclude these from the textual entry
                endTextual = quantBegPos;
                // and try to get an integer from between the parentheses
                try {
                    quant = Integer.parseInt(entryString.substring(
                            quantBegPos + 1, quantEndPos));
                } catch (NumberFormatException oops) {
                    quant = 0;
                }
            }

            final WorkPerformanceMedium perfMedium =
                    new WorkPerformanceMedium(work);
            perfMedium.setText(
                    entryString.substring(0, endTextual).trim());
            if (quant >= 0) {
                perfMedium.setQuantity(quant);
            }
            perfMedium.setVocabulary("aacr2");

            work.getPerformanceMediums().add(perfMedium);

            // reset the beginning consideration (skip over comma)
            begSubstring = endSubstring + 1;
            // if there is more string
            if (begSubstring < subfieldM.length()) {
                // if there is another comma
                endSubstring = subfieldM.indexOf(',', begSubstring);
                // this should exclude a trailing comma,
                // with endSubstring == begSubstring
            } else {
                // past the end
                endSubstring = -1;
            }
        }
    }

    /**
     * Map numericDesignation.
     *
     * @param work WorkJpa work for attribute mapping.
     */
    protected final void mapDesignations(final WorkJpa work) {

        if (this.marcAuthRecord != null) {
            // map from authority record

            final String[] auth1xxFields = {"100", "110", "111", "130"};
            boolean auth1xxHadN = false;

            for (MarcDataField authField :
                    this.marcAuthRecord.getDataFields(auth1xxFields)) {
                // if |n (r)
                for (String subfieldN :
                        authField.getValueList('n')) {

                    auth1xxHadN = true;
                    // is not a date format
                    if (!isDateFormat(subfieldN)) {
                        // use |n
                        final WorkDesignation workDesig =
                                new WorkDesignation(work, subfieldN);

                        work.getDesignations().add(workDesig);
                    }
                }
            }

            // if had no |n
            if (!auth1xxHadN) {
                // use field 4xx
                final String[] auth4xxFields = {
                    "400", "410", "411", "430", "448", "450", "451", "455",
                    "480", "481", "482", "458"};
                for (MarcDataField authField :
                        this.marcAuthRecord.getDataFields(auth4xxFields)) {
                    // subfield |n (R)
                    for (String subfieldN : authField.getValueList('n')) {
                        if (!isDateFormat(subfieldN)) {
                            final WorkDesignation workDesig =
                                    new WorkDesignation(work, subfieldN);

                            work.getDesignations().add(workDesig);
                        }
                    }
                }
            }
        } else {
            // map from bibliographic record

            // if the work comes from
            final String workTitleFieldTag = this.titleDataField.getTag();
            final String[] bibRecs2Map = {
                "130", "240",
                "700", "710", "711", "720", "730",
                "740", "751", "752", "753", "754"};
            boolean workTitleFromBibRecs2Map = false;
            for (String bibRecTag : bibRecs2Map) {
                if (workTitleFieldTag.equals(bibRecTag)) {
                    workTitleFromBibRecs2Map = true;
                }
            }
            if (workTitleFromBibRecs2Map) {
                // and |n (R) not a date format
                for (String subfieldN :
                        this.titleDataField.getValueList('n')) {
                    if (!isDateFormat(subfieldN)) {

                        final WorkDesignation workDesig =
                                new WorkDesignation(work, subfieldN);

                        work.getDesignations().add(workDesig);
                    }
                }
            }
        }
    }

    /**
     *  Helper function for determining
     * whether a subfield matches a date format pattern.
     *
     * @param subfield String subfield to check.
     * @return boolean of subfield matching a date pattern.
     */
    private boolean isDateFormat(final String subfield) {

        boolean matchesPattern = false;

        if (subfield.matches("\\(\\d{4}\\)") //                    (yyyy)
                || subfield.matches("\\(\\d{4}\\-\\d{2}\\)") //    (yyyy-yy)
                || subfield.matches("\\(\\d{4}\\-\\d{4}\\)")) { // (yyyy-yyyy)

            matchesPattern = true;
        }

        return matchesPattern;
    }

    /**
     *  Map key.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapKeys(final WorkJpa work) {

        if (this.marcAuthRecord != null) {
            // use authority record

            // for auth fields
            final String[] authFields = {"100", "110", "111", "130"};
            boolean authFieldsHadR = false;
            boolean authFieldsHadO = false;
            for (MarcDataField authField :
                    this.marcAuthRecord.getDataFields(authFields)) {

                if (authField.hasSubfields("o".toCharArray())) {
                    authFieldsHadO = true;
                }
                if (authField.hasSubfields("r".toCharArray())) {
                    authFieldsHadR = true;
                    // use subfield |r (NR)
                    final WorkKey key =
                            new WorkKey(work, authField.getValue('r'));
                    key.setVocabulary("aacr2");

                    work.getKeys().add(key);
                }
            }

            // if auth fields have no |o and no |r
            if (!authFieldsHadR && !authFieldsHadO) {
                // use 4xx (R) |r (NR)
                final String[] auth4xxFields = {
                    "400", "410", "411", "430", "448",
                    "450", "451", "455",
                    "480", "481", "482", "485"};

                for (MarcDataField field4xx :
                        this.marcAuthRecord.getDataFields(auth4xxFields)) {

                    if (field4xx.hasSubfields("r".toCharArray())) {

                        final WorkKey key =
                                new WorkKey(work, field4xx.getValue('r'));
                        key.setVocabulary("aacr2");

                        work.getKeys().add(key);
                    }
                }
            }
        } else {
            // use bibliographic work title field
            if (this.titleDataField.hasSubfields("r".toCharArray())) {

                final WorkKey key =
                        new WorkKey(work, this.titleDataField.getValue('r'));
                key.setVocabulary("aacr2");

                work.getKeys().add(key);
            }
        }
    }

    /**
     * Map subjectOfTheWork.
     *
     * @param work the WorkJpa work for attribute mapping.
     */
    protected final void mapSubjectHeadings(final WorkJpa work) {

        // for each 6xx
        final String[] subjectFields = {"600", "610", "611", "630", "648",
                                        "650", "651", "653", "654", "655",
                                        "656", "657", "658", "662"};
        for (MarcDataField bibField :
                this.marcBibRecord.getDataFields(subjectFields)) {

            final String fieldTag = bibField.getTag();

            // "map values from each 6xx."
            final WorkSubjectHeading subjHead =
                    new WorkSubjectHeading(work, bibField.concatAllBut(""));

            subjHead.setVocabulary("lcsh");

            // type from mapping spec
            if ("600".equals(fieldTag)) {
                subjHead.setType("person");

            } else if ("650".equals(fieldTag)) {
                subjHead.setType("topic");

            } else if ("651".equals(fieldTag)) {
                subjHead.setType("place");

            } else // type from field name
            if ("610".equals(fieldTag)) {
                subjHead.setType("corporation");

            } else if ("611".equals(fieldTag)) {
                subjHead.setType("meeting");

            } else if ("630".equals(fieldTag)) {
                subjHead.setType("title");

            } else if ("648".equals(fieldTag)) {
                subjHead.setType("chronology");

            } else if ("653".equals(fieldTag)) {
                subjHead.setType("uncontrolled");

            } else if ("654".equals(fieldTag)) {
                subjHead.setType("faceted topic");

            } else if ("655".equals(fieldTag)) {
                subjHead.setType("genre");

            } else if ("656".equals(fieldTag)) {
                subjHead.setType("occupation");

            } else if ("657".equals(fieldTag)) {
                subjHead.setType("function");

            } else if ("658".equals(fieldTag)) {
                subjHead.setType("curriculum objective");

            } else if ("662".equals(fieldTag)) {
                subjHead.setType("hierarchical place");
            }

            work.getSubjectHeadings().add(subjHead);
        }
    }

    /**
     * Map note.
     *
     * @param work WorkJpa work for attribute mapping.
     */
    protected final void mapNotes(final WorkJpa work) {

        // from authority record
        if (this.marcAuthRecord != null) {

            for (MarcDataField field670 :
                    this.marcAuthRecord.getDataFields("670")) {

                final String subfields = field670.concatAllBut("68");

                final WorkNote note = new WorkNote(work, subfields);
                note.setType("sourcedatafound");
                note.setAvailability("public");

                work.getNotes().add(note);
            }

            for (MarcDataField field678 :
                    this.marcAuthRecord.getDataFields("678")) {

                final String subfields = field678.concatAllBut("68");

                final WorkNote note = new WorkNote(work, subfields);
                note.setType("biographicalhistorical");
                note.setAvailability("public");

                work.getNotes().add(note);
            }

            for (MarcDataField field856 :
                    this.marcAuthRecord.getDataFields("856")) {

                final String subfield = field856.getValue('u');

                final WorkNote note = new WorkNote(work, subfield);
                note.setType("electronicresource");
                note.setAvailability("public");

                work.getNotes().add(note);
            }
        }
        // else for a bib record map no note fields
    }

    /**
     * Map Language.
     *
     * @param work WorkJpa work for attribute mapping.
     */
    protected final void mapLanguages(final WorkJpa work) {

        // if an 041 (R)
        if (this.marcBibRecord.hasField("041")) {
            for (MarcDataField field041 :
                    this.marcBibRecord.getDataFields("041")) {
                /*
                 * |d and |h are potentially repeating
                 * first pass handling is to concatenate them
                 */
                final String indicator1 = field041.get1stIndicator();
                if (indicator1.equals("0")) {
                    final String subfieldsD =
                            field041.concatSubfields("d".toCharArray());

                    final WorkLanguage lang =
                            new WorkLanguage(work, subfieldsD);
                    lang.setNormal(subfieldsD);
                    lang.setVocabulary("iso639-2b");

                    work.getLanguages().add(lang);

                } else if (indicator1.equals("1")) {
                    final String subfieldsH =
                            field041.concatSubfields("h".toCharArray());

                    final WorkLanguage lang =
                            new WorkLanguage(work, subfieldsH);
                    lang.setNormal(subfieldsH);
                    lang.setVocabulary("iso638-2b");

                    work.getLanguages().add(lang);
                }
            }
        } else {
            // 008/35-37
            final String langCode =
                    this.marcBibRecord.getControlField("008").getRange(35, 37);
            String langCodeValue =
                    new MarcLanguageCodes().getLanguageCodeProperties().
                    getProperty(langCode);
            if (langCodeValue == null) {
                langCodeValue = "unknown language code";
            }

            final WorkLanguage lang = new WorkLanguage(work, langCodeValue);
            lang.setNormal(langCode);
            lang.setVocabulary("iso639-2b");

            work.getLanguages().add(lang);
        }
    }
}
