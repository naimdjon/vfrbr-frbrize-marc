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
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionDate;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionExtent;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionForm;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionGenre;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionJpa;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionKey;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionLanguage;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionNote;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionPerformanceMedium;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionPerformancePlace;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionScoreType;
import edu.indiana.dlib.vfrbr.persist.entity.expression.ExpressionTitle;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkJpa;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkKey;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkLanguage;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkPerformanceMedium;
import edu.indiana.dlib.vfrbr.persist.entity.work.WorkTitle;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

/**
 *  Field mappings for a Work Expression from a MarcRecord.
 *
 */
final class ExprMap {

    /**
     * The WorkJpa this ExpressionJpa expresses.
     */
    private final WorkJpa work;

    /**
     * The MarcRecord from which to map this Expression.
     */
    private final MarcRecord marcRecord;

    /**
     *  Instantiate an Expresion map
     * for a MarcRecord and a Work.
     *
     * @param sourceMarcRecord source MarcRecord for the Expression fields.
     * @param expressedWork the WorkJpa expressed by this ExpressionJpa.
     */
    protected ExprMap(final MarcRecord sourceMarcRecord,
                      final WorkJpa expressedWork) {

        this.marcRecord = sourceMarcRecord;

        this.work = expressedWork;
    }

    // -- titleOfTheExpression
    /**
     *  Map the ExpressionTitle titles for an ExpressionJpa.
     * Initial implementation is to copy them from the expressed Work.
     *
     * @param owningExpr the ExpressionJpa to which the titles are linked.
     * @return list of ExpressionTitle titles
     * linked to the owning ExpressionJpa.
     */
    protected List<ExpressionTitle> mapTitles(final ExpressionJpa owningExpr) {
        // ExpressionTitle titles
        final List<ExpressionTitle> titles =
                new ArrayList<ExpressionTitle>();

        // first pass implementation: copy from Work
        for (WorkTitle workTitle : this.work.getTitles()) {
            final ExpressionTitle exprTitle =
                    new ExpressionTitle(owningExpr, workTitle.getText());
            exprTitle.setOffset(workTitle.getOffset());

            if ((null != workTitle.getType())
                    && ((workTitle.getType().equals("uniform")
                         || workTitle.getType().equals("variant")))) {

                exprTitle.setVocabulary("naf");
            }

            titles.add(exprTitle);
        }

        return titles;
    }

    // -- formOfExpression
    /**
     *  Map the ExpressionForm forms for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionForm forms
     * linked to the owning expression.
     */
    protected List<ExpressionForm> mapForms(final ExpressionJpa owningExpr) {
        // ExpressionForm forms
        final List<ExpressionForm> forms =
                new ArrayList<ExpressionForm>();
        final char typeChar = this.marcRecord.getTypeChar();

        String exprForm = null;

        if ('j' == typeChar) {

            exprForm = "musical sound";

        } else if ('c' == typeChar) {

            exprForm = "notated music";

        } else if ('d' == typeChar) {

            exprForm = "manuscript notated music";

        } else if ('i' == typeChar) {

            exprForm = "spoken word";

        }

        if (null != exprForm) {
            final ExpressionForm form =
                    new ExpressionForm(owningExpr, exprForm);
            form.setVocabulary("vfrbrformofexpression");
            forms.add(form);
        }

        return forms;
    }

    // -- dateOfExpression
    /**
     *  Map the ExpressionDate dates for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionDate dates
     * linked to the owning expression.
     */
    protected List<ExpressionDate> mapDates(final ExpressionJpa owningExpr) {
        // ExpressionDate dates
        final List<ExpressionDate> dates =
                new ArrayList<ExpressionDate>();

        if (this.marcRecord.hasField("033")) {
            // 033 (R)
            final List<MarcDataField> field033List =
                    this.marcRecord.getDataFields("033");
            for (MarcDataField field033 : field033List) {
                if (field033.hasSubfields("a".toCharArray())) {
                    final String firstIndic = field033.get1stIndicator();
                    if ("0".equals(firstIndic)) {
                        final String normalDate =
                                DateNormalizer.normalizeExpr033Date(
                                field033.getValue('a'));
                        final ExpressionDate exprDate =
                                new ExpressionDate(owningExpr, normalDate);
                        exprDate.setNormal(normalDate);
                        exprDate.setType("single");
                        dates.add(exprDate);
                    } else if ("1".equals(firstIndic)) {
                        for (String field033Date : field033.getValueList('a')) {
                            final String normalDate =
                                    DateNormalizer.normalizeExpr033Date(
                                    field033Date);
                            final ExpressionDate exprDate =
                                    new ExpressionDate(owningExpr, normalDate);
                            exprDate.setNormal(normalDate);
                            exprDate.setType("single");
                            dates.add(exprDate);
                        }
                    } else if ("2".equals(firstIndic)) {
                        // claimed, but not to be trusted, by experience
                        int indx = 0;
                        String normalDate1 = null;
                        String normalDate2 = null;
                        for (String field033Date : field033.getValueList('a')) {
                            if (indx == 0) {
                                normalDate1 =
                                        DateNormalizer.normalizeExpr033Date(
                                        field033Date);
                            }
                            if (indx == 1) {
                                normalDate2 = DateNormalizer.
                                        normalizeExpr033Date(
                                        field033Date);
                            }
                            indx++;
                        }
                        final ExpressionDate exprDate =
                                new ExpressionDate(owningExpr);
                        exprDate.setText(normalDate1 + " to " + normalDate2);
                        exprDate.setNormal(normalDate1 + "/" + normalDate2);
                        exprDate.setType("range");
                        dates.add(exprDate);
                    }
                    // otherwise ??
                }
                // else no 'a' in this field033
            }
        } else if (this.marcRecord.hasField("518")) {
            // 518 (R)
            final List<MarcDataField> field518List =
                    this.marcRecord.getDataFields("518");
            for (MarcDataField field518 : field518List) {
                final ExpressionDate exprDate =
                        new ExpressionDate(owningExpr);
                exprDate.setText(
                        this.marcRecord.getDataField("518").concatAllBut(""));
                dates.add(exprDate);
            }
        }

        return dates;
    }

    // -- languageOfExpression
    /**
     *  Map the ExpressionLanaguage languages
     * for an owning ExpresionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionLanaguage languages
     * linked to the owning ExpressionJpa expression.
     */
    protected List<ExpressionLanguage> mapLanguages(
            final ExpressionJpa owningExpr) {
        // ExpressionLanguage languages
        final List<ExpressionLanguage> languages =
                new ArrayList<ExpressionLanguage>();

        // -- only using the Language attribute of work
        for (WorkLanguage workLanguage : work.getLanguages()) {
            final ExpressionLanguage exprLanguage =
                    new ExpressionLanguage(owningExpr, workLanguage.getText());
            if (null != workLanguage.getNormal()) {
                exprLanguage.setNormal(workLanguage.getNormal());
            }
            if (null != workLanguage.getVocabulary()) {
                exprLanguage.setVocabulary(workLanguage.getVocabulary());
            }
            languages.add(exprLanguage);
        }

        return languages;
    }

    // -- otherDistinguishingCharacteristic
    //  not mapped
    /*
     *  ExpressionCharacteristic characteristics
     */
    // -- extentOfTheExpression
    /**
     *  Map the ExpressionExtent extents for an owning
     * ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionExtent extents
     * linked to the owning expression.
     */
    protected List<ExpressionExtent> mapExtents(
            final ExpressionJpa owningExpr) {
        // ExpressionExtent extents
        final List<ExpressionExtent> extents =
                new ArrayList<ExpressionExtent>();

        // 306 (NR) |a (R)
        if (this.marcRecord.hasField("306")) {
            for (String extentField :
                    this.marcRecord.getDataField("306").getValueList('a')) {
                // hhmmss
                final StringBuilder extent = new StringBuilder();
                final int fieldLength = extentField.length();
                if (fieldLength >= 2) {
                    extent.append(extentField.substring(0, 2));
                } else if (fieldLength == 1) {
                    extent.append("0");
                    extent.append(extentField.substring(0, 1));
                } else {
                    extent.append("00");
                }
                extent.append(":");
                if (fieldLength >= 4) {
                    extent.append(extentField.substring(2, 4));
                } else {
                    extent.append("00");
                }
                extent.append(":");
                if (fieldLength == 6) {
                    extent.append(extentField.substring(4, 6));
                } else if (fieldLength == 5) {
                    extent.append(extentField.substring(4, 5));
                } else {
                    extent.append("00");
                }

                final ExpressionExtent exprExtent =
                        new ExpressionExtent(owningExpr, extent.toString());
                extents.add(exprExtent);
            }
        }

        return extents;
    }

    // -- summarizationOfContent
    //  not mapped
    /*
     * ExpressionSummarization summarizations
     *
     */
    // -- contextForTheExpression
    //  not mapped
    /*
     * ExpressionContext contexts
     *
     */
    // -- criticalResponseToTheExpression
    //  not mapped
    /*
     * ExpressionResponse responses
     *
     */
    // -- typeOfScore
    /*
     * ExpressionScoreType scoreType
     *
     */
    protected List<ExpressionScoreType> mapScoreType(
            final ExpressionJpa owningExpr) {

        final List<ExpressionScoreType> scoreTypes =
                new ArrayList<ExpressionScoreType>();

        if (this.marcRecord.hasField("254")) {
            String scoreTypeText = null;
            // 254 is non repeating field
            // 254|a is non repeating subfield
            scoreTypeText = this.marcRecord.getDataField("254").getValue('a');

            if (null != scoreTypeText) {
                ExpressionScoreType scoreType =
                        new ExpressionScoreType(owningExpr, scoreTypeText);
                scoreTypes.add(scoreType);
            }
        }

        return scoreTypes;
    }

    // -- mediumOfPerformance
    /**
     *  Map the ExpresionPersormanceMedium peformanceMediums
     * for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionPerformanceMedium performanceMediums
     * linked to the owning expression.
     */
    protected List<ExpressionPerformanceMedium> mapPerformanceMediums(
            final ExpressionJpa owningExpr) {
        // ExpressionPerformanceMedium performanceMediums
        final List<ExpressionPerformanceMedium> performanceMediums =
                new ArrayList<ExpressionPerformanceMedium>();

        // first pass implementation: copy from Work
        for (WorkPerformanceMedium workPerfMed :
                this.work.getPerformanceMediums()) {
            final ExpressionPerformanceMedium exprPerfMed =
                    new ExpressionPerformanceMedium(owningExpr,
                                                    workPerfMed.getText());
            exprPerfMed.setQuantity(workPerfMed.getQuantity());
            exprPerfMed.setVocabulary(workPerfMed.getVocabulary());

            performanceMediums.add(exprPerfMed);
        }

        return performanceMediums;
    }

    // -- note
    /**
     *  Map the ExpressionNote notes for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionNote notes
     * linked to the owning expression.
     */
    protected List<ExpressionNote> mapNotes(final ExpressionJpa owningExpr) {
        // ExpressionNote notes
        final List<ExpressionNote> notes = new ArrayList<ExpressionNote>();

        // 500 (R)
        for (MarcDataField field500 : this.marcRecord.getDataFields("500")) {
            final ExpressionNote note =
                    new ExpressionNote(owningExpr, field500.concatAllBut(""));
            note.setAvailability("public");

            notes.add(note);
        }

        // 511 (R)
        for (MarcDataField field511 : this.marcRecord.getDataFields("511")) {
            final ExpressionNote note =
                    new ExpressionNote(owningExpr, field511.concatAllBut(""));
            note.setAvailability("public");

            notes.add(note);
        }

        return notes;
    }

    // -- placeOfPerformance
    /**
     *  Map the ExpressionPerformancePlace performancePlaces
     * for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionPersormancePlace performancePlaces
     * linked to the owning expression.
     */
    protected List<ExpressionPerformancePlace> mapPerformancePlace(
            final ExpressionJpa owningExpr) {
        // ExpressionPerformancePlace performancePlace
        final List<ExpressionPerformancePlace> performancePlace =
                new ArrayList<ExpressionPerformancePlace>();

        // FIXME Implement placeOfPerformace mapping from 033 in addition to 518
        // Cutter mapping ???  (3030 pages of it)
        // first pass, just the 518 (R) field(s)
        for (MarcDataField field518 : this.marcRecord.getDataFields("518")) {
            final ExpressionPerformancePlace exprPerfPlace =
                    new ExpressionPerformancePlace(owningExpr);
            exprPerfPlace.setText(field518.concatAllBut(""));

            performancePlace.add(exprPerfPlace);
        }

        return performancePlace;
    }

    // -- key
    /**
     *  Map the ExpressionKey keys for an owning ExpressionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionKey keys
     * linked to the owning expression.
     */
    protected List<ExpressionKey> mapKeys(final ExpressionJpa owningExpr) {
        // ExpressionKey keys
        final List<ExpressionKey> keys = new ArrayList<ExpressionKey>();

        for (WorkKey workKey : work.getKeys()) {
            final ExpressionKey exprKey = new ExpressionKey(owningExpr);
            exprKey.setText(workKey.getText());
            exprKey.setVocabulary("aacr2");

            keys.add(exprKey);
        }

        return keys;
    }

    // -- genreFormStyle
    /**
     *  Map the ExpressionGenre genreFormStyles for
     * an owning ExpresionJpa expression.
     *
     * @param owningExpr the owning ExpressionJpa expression.
     * @return list of ExpressionGenre genreFormStyles
     * linked to the owning expression.
     */
    protected List<ExpressionGenre> mapGenres(final ExpressionJpa owningExpr) {
        // ExpressionGenre genres
        final List<ExpressionGenre> genres = new ArrayList<ExpressionGenre>();

        // if 008/18-19 (NR)
        if (this.marcRecord.hasField("008")) {
            final String compFormCode =
                    this.marcRecord.getControlField("008").getRange(18, 19);
            if (!"mu".equals(compFormCode)) {
                // if 008/18-19 not "mu", map code replacement string
                final Properties compFormCodeProps =
                        new MarcCompositionFormCodes().
                        getCompositionFormCodeProperties();
                final ExpressionGenre genre = new ExpressionGenre(
                        owningExpr,
                        compFormCodeProps.getProperty(compFormCode));
                genre.setVocabulary("marcformofcomposition");

                genres.add(genre);
            }
        } else { // else 047 (NR) |a (R)
            if (this.marcRecord.hasField("047")) {
                // has a 047, get the mapping property
                final Properties compFormCodeProps =
                        new MarcCompositionFormCodes().
                        getCompositionFormCodeProperties();
                // and map all the |a codes to values
                for (String compFormCode :
                        this.marcRecord.getDataField("047").getValueList('a')) {
                    final ExpressionGenre genre = new ExpressionGenre(
                            owningExpr,
                            compFormCodeProps.getProperty(compFormCode));
                    genre.setVocabulary("marcformofcomposition");

                    genres.add(genre);
                }
            }
        }

        return genres;
    }
}
