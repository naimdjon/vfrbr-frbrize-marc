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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcControlField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcCountryCodes;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcLanguageCodes;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;

import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationAccessAddress;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationAccessMode;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationAccompanyingLanguage;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationCaptureMode;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationCarrierDimension;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationCarrierExtent;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationCarrierForm;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationDesignation;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationIdentifier;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationNote;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPhysicalMedium;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPlayingSpeed;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPublicationDate;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPublicationJpa;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPublicationPlace;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationPublicationPublisher;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationReproductionCharacteristic;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationResponsibility;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationSeriesStatement;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationSoundKind;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationTapeConfiguration;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationTitle;

import java.util.List;
import java.util.Properties;


/**
 *  Implementation mappings from a MarcRecord to a Manifestation.
 *
 */
final class ManifMap {

    private final static String NAXOS_URL_FRAG = "iub.naxosmusiclibrary.com";

    private final static String DRAM_URL_FRAG = "proxy.dramonline.org";

    private final static String VARIATIONS_URL_FRAG1 = "purl.dlib.indiana.edu";

    private final static String VARIATIONS_URL_FRAG2 = "music.indiana.edu";

    private final static String IU_CAT_URL_BASE =
            "http://purl.dlib.indiana.edu/iudl/iucat/";

    private final MarcRecord marcRecord;

    protected ManifMap(MarcRecord marcRecord) {
        this.marcRecord = marcRecord;
    }

    /**
     *  formOfExpression
     * Denormalized from Expression to Manifestation
     * for performant determination of recording or score
     * and for Manifestations with no Work/Expression
     *
     * @param manif the ManifestationJpa manifestation
     */
    protected void mapFormOfExpression(final ManifestationJpa manif) {

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
            manif.setFormOfExpression(exprForm);
        }
    }


    /**
     *  titleOfTheManifestation.
     *
     * @param manif the ManifestationJpa manifestation
     */
    protected void mapTitles(final ManifestationJpa manif) {

        final ManifestationTitle manifTitle = new ManifestationTitle(manif);

        if (this.marcRecord.hasField("245")) {
            final MarcDataField titleField =
                    this.marcRecord.getDataField("245");
            manifTitle.setText(titleField.concatAllBut("ch"));
            int offset = 0;
            try {
                offset = Integer.valueOf(titleField.get2ndIndicator());
            } catch (NumberFormatException ex) {
                // leave offset of zero
            }
            manifTitle.setOffset(offset);
            manifTitle.setType("transcribed");
        } else {
            manifTitle.setType("supplied");
        }

        manif.getTitles().add(manifTitle);
    }

    /**
     *  statementOfResponsibility.
     *
     * @param manif the ManifestationJpa manifestation
     */
    protected void mapResponsibilities(final ManifestationJpa manif) {

        if (this.marcRecord.hasField("245")) {
            final MarcDataField field245 = this.marcRecord.getDataField("245");
            if (field245.hasSubfields("c".toCharArray())) {
                final ManifestationResponsibility manifResp =
                        new ManifestationResponsibility(
                        manif,
                        field245.getValue('c'));

                manif.getResponsibilities().add(manifResp);
            }
        }
    }

    /**
     *  editionIssueDesignation.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapDesignations(final ManifestationJpa manif) {

        if (this.marcRecord.hasField("250")) {
            final MarcDataField field250 = this.marcRecord.getDataField("250");

            final ManifestationDesignation manifDesig =
                    new ManifestationDesignation(
                    manif,
                    field250.concatSubfields("ab".toCharArray()));

            manif.getDesignations().add(manifDesig);
        }
    }

    /**
     *  PublicationDistribution:
     * placeOfPublicationDistribution,
     * publisherDistributor,
     * dateOfPublicationDistribution.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapPublications(final ManifestationJpa manif) {

        /*
         * Mapping adjusted to "merge" 260 and 008 into "value" and "normal"
         */

        // -- 260 Publication (R) && 008 Publication (NR)
        // 260 could repeat, although allegedly none do
        // use only "first" 260 field

        /*
         * Create a candidate manifPub.
         * Use boolean flag to track creation of any field values.
         * Do not add an "empty" manifPub to manif.
         */
        final ManifestationPublicationJpa manifPub =
                new ManifestationPublicationJpa(manif);
        manifPub.setListOrder(0);
        boolean hasFieldAdded = false;

        final MarcDataField field260 =
                this.marcRecord.getDataField("260");
        final MarcControlField field008 =
                this.marcRecord.getControlField("008");

        // -- place
        if ((null != field260) && (field260.hasSubfields("a".toCharArray()))) {

            final ManifestationPublicationPlace manifPubPlace =
                    new ManifestationPublicationPlace(manifPub);
            manifPubPlace.setText(field260.getValue('a'));
            manifPubPlace.setType("publication");
            manifPub.setPlace(manifPubPlace);
            hasFieldAdded = true;


        } else if ((null != field008) && (field008.hasRange(15, 17))) {

            // no 260|a, map from 008 (converting code to country)
            final ManifestationPublicationPlace manifPubPlace =
                    new ManifestationPublicationPlace(manifPub);
            final Properties countryCodes =
                    new MarcCountryCodes().getCountryCodeProperties();
            manifPubPlace.setText(
                    countryCodes.getProperty(field008.getRange(15, 17)));
            manifPubPlace.setType("publication");
            manifPubPlace.setJurisdiction("country");
            manifPubPlace.setVocabulary("???");
            manifPub.setPlace(manifPubPlace);
            hasFieldAdded = true;

        }
        // otherwise map nothing for place

        // -- publisher
        if ((null != field260) && (field260.hasSubfields("b".toCharArray()))) {

            final ManifestationPublicationPublisher manifPubPub =
                    new ManifestationPublicationPublisher(manifPub);
            manifPubPub.setText(field260.getValue('b'));
            manifPubPub.setType("publisher");
            manifPub.setPublisher(manifPubPub);
            hasFieldAdded = true;

        } else if (this.marcRecord.hasField("028")) {

            final MarcDataField field028 =
                    this.marcRecord.getDataField("028");

            if ((!field028.get1stIndicator().equals("0"))
                    && (field028.hasSubfields("b".toCharArray()))) {

                final ManifestationPublicationPublisher manifPubPub =
                        new ManifestationPublicationPublisher(manifPub);
                manifPubPub.setText(field028.getValue('b'));
                manifPubPub.setType("publisher");
                manifPub.setPublisher(manifPubPub);
                hasFieldAdded = true;

            }
        }
        // otherwise map nothing for place

        // -- date
        final ManifestationPublicationDate manifPubDate =
                new ManifestationPublicationDate(manifPub);
        boolean hasPubDate = false;

        // text value
        if ((null != field260) && (field260.hasSubfields("c".toCharArray()))) {

            manifPubDate.setText(field260.getValue('c'));
            hasPubDate = true;

        } else if ((null != field008) && (field008.hasRange(07, 10))) {

            manifPubDate.setText(field008.getRange(07, 10));
            hasPubDate = true;

        }
        // normal value
        if ((null != field008) && (field008.hasRange(07, 10))) {

            manifPubDate.setNormal(field008.getRange(07, 10));

        }

        if (hasPubDate) {
            manifPub.setDate(manifPubDate);
            hasFieldAdded = true;
        }

        // don't add empty publication to manifestation
        if (hasFieldAdded) {
            manif.getPublications().add(manifPub);
        }

    } // of mapPublications

    /**
     *  fabricatorManufacturer -- not mapped.
     *
     * ManifestationFabricator
     */
    /**
     *  seriesStatement.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapSeriesStatements(final ManifestationJpa manif) {

        final String[] seriesFieldNums = {"440", "490", "800", "810", "811",
                                          "830"};

        for (MarcDataField seriesField :
                this.marcRecord.getDataFields(seriesFieldNums)) {

            final String seriesStmtString = seriesField.concatAllBut("x468");
            if (!"".equals(seriesStmtString)) {
                final ManifestationSeriesStatement seriesStmt =
                        new ManifestationSeriesStatement(manif);
                seriesStmt.setText(seriesStmtString);

                manif.getSeriesStmts().add(seriesStmt);
            }
        }
    }

    /**
     *  formOfCarrier.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapCarrierForms(final ManifestationJpa manif) {

        for (MarcControlField field007 :
                this.marcRecord.getControlFields("007")) {
            if (field007.hasRange(1, 1)) {
                // get the MARC designator
                final String materialDesig = field007.getRange(1, 1);
                // holder for translated material
                String material = null;
                ManifestationCarrierForm carrierForm = null;
                // see if we recognize the code
                if ("d".equals(materialDesig)) {
                    material = "Sound disc";
                } else if ("e".equals(materialDesig)) {
                    material = "Cylinder";
                } else if ("g".equals(materialDesig)) {
                    material = "Sound cartridge";
                } else if ("i".equals(materialDesig)) {
                    material = "Sound-track film";
                } else if ("q".equals(materialDesig)) {
                    material = "Roll";
                } else if ("s".equals(materialDesig)) {
                    material = "Sound cassette";
                } else if ("t".equals(materialDesig)) {
                    material = "Sount-tape reel";
                } else if ("u".equals(materialDesig)) {
                    material = "Unspecified";
                } else if ("w".equals(materialDesig)) {
                    material = "Wire recording";
                } else if ("z".equals(materialDesig)) {
                    material = "Other";
                } else if ("|".equals(materialDesig)) {
                    material = "No Attempt to code";
                }
                // if we got a recongnized designator code
                if (null != material) {

                    carrierForm = new ManifestationCarrierForm(manif);
                    carrierForm.setVocabulary("marcmaterial");
                    carrierForm.setText(material);

                    manif.getCarrierForms().add(carrierForm);

                }
            }
        }
    }

    /**
     *  extentOfTheCarrier.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapCarrierExtents(final ManifestationJpa manif) {

        // note: inclusion of units (|f) is an extention to the mapping specification

        for (MarcDataField carrierField :
                this.marcRecord.getDataFields("300")) {

            final String carrierFieldString =
                    carrierField.concatSubfields("af".toCharArray());
            if (!"".equals(carrierFieldString)) {
                final ManifestationCarrierExtent manifCarrierExtent =
                        new ManifestationCarrierExtent(manif);
                manifCarrierExtent.setText(carrierFieldString);

                manif.getCarrierExtents().add(manifCarrierExtent);
            }
        }
    }

    /**
     *  physicalMedium.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapPhysicalMediums(final ManifestationJpa manif) {
        // 007 (R)

        for (MarcControlField field007 :
                this.marcRecord.getControlFields("007")) {

            String desig = "";
            if (field007.hasRange(1, 1)) {
                desig = field007.getRange(1, 1);  // specific material designation
            }

            String speed = "";
            if (field007.hasRange(3, 3)) {
                speed = field007.getRange(3, 3);  // speed
            }

            String kind = "";
            if (field007.hasRange(10, 10)) {
                kind = field007.getRange(10, 10); // kind of material
            }

            String value = null;

            if ("d".equals(desig)) {
                if ("f".equals(speed)) {
                    // (desig:d & speed:f)  Plastic with metal
                    value = "Plastic with metal";
                } else if ("d".equals(speed)) {
                    // (desig:d & speed:d)  Shellac
                    value = "Shellac";
                } else if ("a".equals(speed)
                        || "b".equals(speed)
                        || "c".equals(speed)) {
                    // (desig:d & speed:[a|b|c])  Plastic
                    value = "Plastic";
                }
                // otherwise skip
            } else if ("s".equals(desig)
                    || "g".equals(desig)) {
                // (desig:[s|g]) Plastic
                value = "Plastic";
            } else if ("a".equals(kind)) {
                // (kind:a) Lacquer coating
                value = "Lacquer coating";
            } else if ("b".equals(kind)) {
                // (kind:b) Cellulose nitrate
                value = "Cellulose nitrate";
            } else if ("c".equals(kind)) {
                // (kind:c) Acetate tape with ferrous oxide
                value = "Acetate tape with ferrous oxide";
            } else if ("g".equals(kind)) {
                // (kind:g) Glass with lacquer
                value = "Glass with lacquer";
            } else if ("i".equals(kind)) {
                // (kind:i) Aluminum with lacquer
                value = "Aluminum with lacquer";
            } else if ("l".equals(kind)) {
                // (kind:l) Metal
                value = "Metal";
            } else if ("r".equals(kind)) {
                // (kind:r) Paper with lacquer or ferrous oxide
                value = "Paper with lacquer or ferrous oxide";
            } else if ("u".equals(kind)) {
                // (kind:u) Unknown
                value = "Unknown";
            } else if ("w".equals(kind)) {
                // (kind:w) Wax
                value = "Wax";
            }
            // otherwise skip

            if (value != null) {
                final ManifestationPhysicalMedium physicalMedium =
                        new ManifestationPhysicalMedium(manif, value);
                manif.getPhysicalMediums().add(physicalMedium);
            }
        }
    }

    /**
     *  captureMode.
     *
     * 007 (R) 13
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapCaptureModes(final ManifestationJpa manif) {

        for (MarcControlField field007 : this.marcRecord.getControlFields("007")) {

            if (field007.hasRange(13, 13)) {

                final String modeValue = field007.getRange(13, 13);
                String mode = null;

                if ("a".equals(modeValue)) {
                    mode = "Acoustical capture, direct storage";
                } else if ("b".equals(modeValue)) {
                    mode = "Direct storage, not acoustical";
                } else if ("d".equals(modeValue)) {
                    mode = "Digital storage";
                } else if ("e".equals(modeValue)) {
                    mode = "Analog electrical storage";
                } else if ("u".equals(modeValue)) {
                    mode = "Unknown";
                } else if ("z".equals(modeValue)) {
                    mode = "Other";
                } else if ("|".equals(modeValue)) {
                    mode = "No attempt to code";
                }

                if (null != mode) {

                    final ManifestationCaptureMode captureMode =
                            new ManifestationCaptureMode(manif);

                    captureMode.setText(mode);
                    captureMode.setVocabulary("marccapture");

                    manif.getCaptureModes().add(captureMode);

                }
            }
        }
    }

    /**
     *  dimensionsOfTheCarrier.
     *
     * 007 (R) 06
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapCarrierDimensions(final ManifestationJpa manif) {

        for (MarcControlField field007 :
                this.marcRecord.getControlFields("007")) {
            if (field007.hasRange(6, 6)) {

                final String dimensionCode = field007.getRange(6, 6);
                String dimension = null;

                if ("a".equals(dimensionCode)) {
                    dimension = "3 in. diameter";
                } else if ("b".equals(dimensionCode)) {
                    dimension = "5 in. diameter";
                } else if ("c".equals(dimensionCode)) {
                    dimension = "7 in. diameter";
                } else if ("d".equals(dimensionCode)) {
                    dimension = "10 in. diameter";
                } else if ("e".equals(dimensionCode)) {
                    dimension = "12 in. diameter";
                } else if ("f".equals(dimensionCode)) {
                    dimension = "16 in. diameter";
                } else if ("g".equals(dimensionCode)) {
                    dimension = "4 3/4 in. or 12 cm. diameter";
                } else if ("j".equals(dimensionCode)) {
                    dimension = "3 7/8 x 2 1/2 in.";
                } else if ("n".equals(dimensionCode)) {
                    dimension = "Not applicable";
                } else if ("o".equals(dimensionCode)) {
                    dimension = "5 1/4 x 3 7/8 in.";
                } else if ("s".equals(dimensionCode)) {
                    dimension = "2 3/4 x 4 in.";
                } else if ("u".equals(dimensionCode)) {
                    dimension = "Unknown";
                } else if ("z".equals(dimensionCode)) {
                    dimension = "Other";
                } else if ("|".equals(dimensionCode)) {
                    dimension = "No attempt to code";
                }

                if (dimension != null) {

                    final ManifestationCarrierDimension carrierDimension =
                            new ManifestationCarrierDimension(manif, dimension);

                    manif.getCarrierDimensions().add(carrierDimension);
                }
            }
        }
    }

    /**
     *  manifestationIdentifier.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapIdentifiers(final ManifestationJpa manif) {

        ManifestationIdentifier identifier = null;

        // 024 (R) |a (NR) if ind-1 is (1 or 3)
        final List<MarcDataField> field024List =
                this.marcRecord.getDataFields("024");
        for (MarcDataField field024 : field024List) {
            if (field024.get1stIndicator().equals("1")) {
                identifier = new ManifestationIdentifier(manif);
                identifier.setText(field024.getValue('a'));
                identifier.setType("upc");
                manif.getIdentifiers().add(identifier);
            } else if (field024.get1stIndicator().equals("3")) {
                identifier = new ManifestationIdentifier(manif);
                identifier.setText(field024.getValue('a'));
                identifier.setType("ean");
                manif.getIdentifiers().add(identifier);
            }
        }

        // 028 (R) |a|b (NR)
        final List<MarcDataField> field028List =
                this.marcRecord.getDataFields("028");
        for (MarcDataField field028 : field028List) {
            if (field028.get1stIndicator().equals("0")) {
                identifier = new ManifestationIdentifier(manif);
                identifier.setText(
                        field028.getValue('b') + " : "
                        + field028.getValue('a'));
                identifier.setType("publicationnumber");
                manif.getIdentifiers().add(identifier);
            } else if (field028.get1stIndicator().equals("1")) {
                identifier = new ManifestationIdentifier(manif);
                identifier.setText(
                        field028.getValue('b') + " : "
                        + field028.getValue('a'));
                identifier.setType("matrixnumber");
                manif.getIdentifiers().add(identifier);
            }
        }

        // 035 (R) |a (NR)
        final List<MarcDataField> field035List =
                this.marcRecord.getDataFields("035");
        for (MarcDataField field035 : field035List) {
            identifier = new ManifestationIdentifier(manif);
            identifier.setText(field035.getValue('a'));
            identifier.setType("oclnumber");
            manif.getIdentifiers().add(identifier);
        }
    }

    /**
     *  sourceForAcquisitionAccessAuthorization -- not mapped
     *
     * ManifestationAAASource
     */
    /**
     *  accessRestrictionsOnTheManifestation -- not mapped
     *
     * ManifestationAccessRestriction
     */
    /**
     *  playingSpeed.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapPlayingSpeeds(final ManifestationJpa manif) {
        // 007 (R) 03
        for (MarcControlField field007 : this.marcRecord.getControlFields("007")) {

            if (field007.hasRange(3, 3)) {

                final String speedCode = field007.getRange(3, 3);
                String speedText = null;

                if ("a".equals(speedCode)) {
                    speedText = "16 rpm (disks)";
                } else if ("b".equals(speedCode)) {
                    speedText = "33 1/3 rpm (disks)";
                } else if ("c".equals(speedCode)) {
                    speedText = "45 rpm (disks)";
                } else if ("d".equals(speedCode)) {
                    speedText = "78 rpm (disks)";
                } else if ("e".equals(speedCode)) {
                    speedText = "8 rpm (disks)";
                } else if ("f".equals(speedCode)) {
                    speedText = "1.4 m. per second (disks)";
                } else if ("h".equals(speedCode)) {
                    speedText = "120 rpm (cylinders)";
                } else if ("i".equals(speedCode)) {
                    speedText = "160 rpm (cylinders)";
                } else if ("k".equals(speedCode)) {
                    speedText = "15/16 ips (tapes)";
                } else if ("l".equals(speedCode)) {
                    speedText = "1 7/8 ips (tapes)";
                } else if ("m".equals(speedCode)) {
                    speedText = "3 3/4 ips (tapes)";
                } else if ("o".equals(speedCode)) {
                    speedText = "7 1/2 ips (tapes)";
                } else if ("p".equals(speedCode)) {
                    speedText = "15 ips (tapes)";
                } else if ("r".equals(speedCode)) {
                    speedText = "30 ips (tape)";
                } else if ("u".equals(speedCode)) {
                    speedText = "Unknown";
                } else if ("z".equals(speedCode)) {
                    speedText = "Other";
                } else if ("|".equals(speedCode)) {
                    speedText = "No attempt to code";
                }

                if (null != speedText) {

                    final ManifestationPlayingSpeed playingSpeed =
                            new ManifestationPlayingSpeed(manif, speedText);

                    playingSpeed.setVocabulary("marcspeed");

                    manif.getPlayingSpeeds().add(playingSpeed);
                }
            }
        }
    }

    /**
     *  tapeConfiguration.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapTapeConfigurations(final ManifestationJpa manif) {
        // 007 (R) 08
        for (MarcControlField field007 : this.marcRecord.getControlFields("007")) {

            if (field007.hasRange(8, 8)) {

                final String configCode = field007.getRange(8, 8);
                String configText = null;

                if ("a".equals(configCode)) {
                    configText = "Full (1) track";
                } else if ("b".equals(configCode)) {
                    configText = "Half (2) track";
                } else if ("c".equals(configCode)) {
                    configText = "Quarter (4) track";
                } else if ("d".equals(configCode)) {
                    configText = "Eight track";
                } else if ("e".equals(configCode)) {
                    configText = "Twelve track";
                } else if ("f".equals(configCode)) {
                    configText = "Sixteen track";
                } else if ("n".equals(configCode)) {
                    configText = "Not applicable";
                } else if ("u".equals(configCode)) {
                    configText = "Unknown";
                } else if ("z".equals(configCode)) {
                    configText = "Other";
                } else if ("|".equals(configCode)) {
                    configText = "No attempt to code";
                }

                if (null != configText) {

                    final ManifestationTapeConfiguration tapeConfig =
                            new ManifestationTapeConfiguration(manif, configText);

                    tapeConfig.setVocabulary("marctapeconfiguration");

                    manif.getTapeConfigs().add(tapeConfig);
                }
            }
        }
    }

    /**
     *  kindOfSound.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapSoundKinds(final ManifestationJpa manif) {
        // 007 (R) 04
        for (MarcControlField field007 : this.marcRecord.getControlFields("007")) {

            if (field007.hasRange(4, 4)) {

                final String kindCode = field007.getRange(4, 4);
                String kindText = null;

                if ("m".equals(kindCode)) {
                    kindText = "Monaural";
                } else if ("q".equals(kindCode)) {
                    kindText = "Quadraphonic";
                } else if ("s".equals(kindCode)) {
                    kindText = "Stereophonic";
                } else if ("u".equals(kindCode)) {
                    kindText = "Unknown";
                } else if ("z".equals(kindCode)) {
                    kindText = "Other";
                } else if ("|".equals(kindCode)) {
                    kindText = "No attempt to code";
                }

                if (null != kindText) {

                    final ManifestationSoundKind soundKind =
                            new ManifestationSoundKind(manif, kindText);

                    soundKind.setVocabulary("marcplaybackchannel");

                    manif.getSoundKinds().add(soundKind);
                }
            }
        }
    }

    /**
     *  specialReproductionCharacteristics.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapReproCharacteristics(final ManifestationJpa manif) {
        // 007 (R) 12
        for (MarcControlField field007 : this.marcRecord.getControlFields("007")) {

            if (field007.hasRange(12, 12)) {

                final String reproCharCode = field007.getRange(12, 12);
                String reproCharText = null;

                if ("a".equals(reproCharCode)) {
                    reproCharText = "NAB standard";
                } else if ("b".equals(reproCharCode)) {
                    reproCharText = "CCIR standard";
                } else if ("c".equals(reproCharCode)) {
                    reproCharText = "Dolby-B encoded";
                } else if ("d".equals(reproCharCode)) {
                    reproCharText = "dbx encoded";
                } else if ("e".equals(reproCharCode)) {
                    reproCharText = "Digital recording";
                } else if ("f".equals(reproCharCode)) {
                    reproCharText = "Dolby-A encoded";
                } else if ("g".equals(reproCharCode)) {
                    reproCharText = "Dolby-C encoded";
                } else if ("h".equals(reproCharCode)) {
                    reproCharText = "CX encoded";
                } else if ("n".equals(reproCharCode)) {
                    reproCharText = "Not applicable";
                } else if ("u".equals(reproCharCode)) {
                    reproCharText = "Unknown";
                } else if ("z".equals(reproCharCode)) {
                    reproCharText = "Other";
                } else if ("|".equals(reproCharCode)) {
                    reproCharText = "No attempt to code";
                }

                if (null != reproCharText) {

                    final ManifestationReproductionCharacteristic reproChar =
                            new ManifestationReproductionCharacteristic(manif,
                                                                        reproCharText);

                    reproChar.setVocabulary("marcspecialplayback");

                    manif.getReproChars().add(reproChar);
                }
            }
        }
    }

    /**
     *  fileCharacteristics -- not mapped
     *
     * ManifestationFileCharacteristic
     */
    /**
     *  modeOfAccess.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapAccessModes(final ManifestationJpa manif) {
        /*
         * this mapping is in addition to the Spring2010 mapping
         * which does not map any fields for modeOfAccess
         */
        for (MarcDataField currentField :
                this.marcRecord.getDataFields("856")) {

            final String url = currentField.concatSubfields("u".toCharArray());

            if (url.contains(ManifMap.DRAM_URL_FRAG)
                    || url.contains(ManifMap.NAXOS_URL_FRAG)
                    || url.contains(ManifMap.VARIATIONS_URL_FRAG1)
                    || url.contains(ManifMap.VARIATIONS_URL_FRAG2)) {

                final ManifestationAccessMode mam =
                        new ManifestationAccessMode(manif, "online");

                manif.getAccessModes().add(mam);
            }
        }
    }

    /**
     *  accessAddress.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapAccessAddresses(final ManifestationJpa manif) {
        /*
         * this mapping extends the Spring2010 mapping specification
         * which only maps from 856|u without type
         */

        //set IU cat URL
        if (!"".equals(this.marcRecord.getControlNumber())) {
            final String iuCatURL =
                    ManifMap.IU_CAT_URL_BASE
                    + this.marcRecord.getControlNumber();
            final ManifestationAccessAddress maa =
                    new ManifestationAccessAddress(manif, iuCatURL);
            maa.setType("iucat");

            manif.getAccessAddresses().add(maa);
        }

        //set online access URLs
        for (MarcDataField currentField :
                this.marcRecord.getDataFields("856")) {
            final String url = currentField.concatSubfields("u".toCharArray());

            if (url.contains(ManifMap.DRAM_URL_FRAG)) {
                final ManifestationAccessAddress maa =
                        new ManifestationAccessAddress(manif, url);
                maa.setType("dram");
                manif.getAccessAddresses().add(maa);
            } else if (url.contains(ManifMap.NAXOS_URL_FRAG)) {
                final ManifestationAccessAddress maa =
                        new ManifestationAccessAddress(manif, url);
                maa.setType("naxos");
                manif.getAccessAddresses().add(maa);
            } else if (url.contains(ManifMap.VARIATIONS_URL_FRAG1)
                    || url.contains(ManifMap.VARIATIONS_URL_FRAG2)) {
                final ManifestationAccessAddress maa =
                        new ManifestationAccessAddress(manif, url);
                maa.setType("variations");
                manif.getAccessAddresses().add(maa);
            }
        }
    }

    /**
     *  note.
     *
     * @param manif ManifestationJpa manifestation
     */
    // Spring 2010 spec: "Map value from 505."
    protected void mapNotes(final ManifestationJpa manif) {

        for (MarcDataField currentNote :
                this.marcRecord.getDataFields("505")) {

            final String noteValue = currentNote.concatAllBut("");

            if (!noteValue.isEmpty()) {

                final ManifestationNote note =
                        new ManifestationNote(manif, noteValue);
                manif.getNotes().add(note);
            }
        }
    }

    /**
     *  languageOfAccompanyingMaterials.
     *
     * @param manif ManifestationJpa manifestation
     */
    protected void mapAccompLangs(final ManifestationJpa manif) {
        // 041 (R) |b (R), |e (R), |g (R)

        Properties languageCodes = null;

        // 041 (R)
        final List<MarcDataField> field041List =
                this.marcRecord.getDataFields("041");
        if (!field041List.isEmpty()) {
            // load the languageCodes
            final MarcLanguageCodes marcLanguageCodes =
                    new MarcLanguageCodes();
            languageCodes = marcLanguageCodes.getLanguageCodeProperties();
        }

        for (MarcDataField field041 : field041List) {
            // |b (R), |e (R), |g (R) -- as list, not concatenation
            final List<String> langCodes = field041.getValueList('b');
            langCodes.addAll(field041.getValueList('e'));
            langCodes.addAll(field041.getValueList('g'));

            for (String langCode : langCodes) {

                // only save languages we recognize
                // Properties returns null if not found
                String language = (String) languageCodes.get(langCode);

                if (null != language) {

                    final ManifestationAccompanyingLanguage accompLang =
                            new ManifestationAccompanyingLanguage(manif);

                    accompLang.setText(language);
                    accompLang.setNormal(langCode);
                    accompLang.setVocabulary("iso639-2b");

                    manif.getAccompLangs().add(accompLang);
                }
            }
        }
    }
}
