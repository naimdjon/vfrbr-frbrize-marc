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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.ManifestationDAO;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import org.apache.log4j.Logger;

/**
 * Map a Manifestation from a MarcRecord.
 *
 */
public class ManifestationMapper {

    private static Logger log = Logger.getLogger(ManifestationMapper.class);

    final String NAXOS_URL_FRAG = "iub.naxosmusiclibrary.com";

    final String DRAM_URL_FRAG = "proxy.dramonline.org";

    final String VARIATIONS_URL_FRAG1 = "purl.dlib.indiana.edu";

    final String VARIATIONS_URL_FRAG2 = "music.indiana.edu";

    private MarcRecord marcRecord;

    public ManifestationMapper(MarcRecord marcRecord) {
        this.marcRecord = marcRecord;
    }

    /**
     *  Get a ManifestationJpa from a MarcRecord.
     * The MarcRecord is set by parameter to the constructor.
     *
     * @param manifDAO a ManifestationDAO containing an active
     * EntityManager context.
     * @return a newly instantiated and mapped ManifestationJpa.
     */
    public final ManifestationJpa getManifestation(
            final ManifestationDAO manifDAO) {

        if (log.isInfoEnabled()) {
            log.info("Instantiating new Manifestation:");
        }

        final ManifestationJpa manif = manifDAO.getNew();

        final ManifMap manifMap = new ManifMap(this.marcRecord);

        // -- formOfTheExpression
        manifMap.mapFormOfExpression(manif);

        // -- titleOfTheManifestation
        //  List<ManifestationTitle> titles
        manifMap.mapTitles(manif);

        // -- statementOfReponsibility
        //  <ManifestationResponsibility> responsibilities
        manifMap.mapResponsibilities(manif);

        // -- editionIssueDesignation
        //  List<ManifestationDesignation> designations
        manifMap.mapDesignations(manif);

        // -- publication
        //  -- placeOfPublicationDistribution
        //  -- publisherDistributor
        //  -- dateOfPublicationDistribution
        //  List<ManifestationPublicationJpa> publications
        manifMap.mapPublications(manif);

        // -- fabricatorManufacturer
        //  List<ManifestationFabricator> fabricators
        // not mapped

        // -- seriesStatement
        //  List<ManifestationSeriesStatement> seriesStmts
        manifMap.mapSeriesStatements(manif);

        // -- formOfCarrier
        //  List<ManifestationCarrierForm> carrierForms
        manifMap.mapCarrierForms(manif);

        // -- extentOfTheCarrier
        //  List<ManifestationCarrierExtent> carrierExtents
        manifMap.mapCarrierExtents(manif);

        // -- physicalMedium
        //  List<ManifestationPhysicalMedium> physicalMediums
        manifMap.mapPhysicalMediums(manif);

        // -- captureMode
        //  List<ManifestationCaptureMode> captureModes
        manifMap.mapCaptureModes(manif);

        // -- dimensionsOfTheCarrier
        //  List<ManifestationCarrierDimension> carrierDimensions
        manifMap.mapCarrierDimensions(manif);

        // -- manifestationIdentifier
        //  List<ManifestationIdentifier> identifiers
        manifMap.mapIdentifiers(manif);

        // -- sourceForAcquisitionAccessAuthority
        //  List<ManifestationAAASource> acqAccAuthSources
        // not mapped

        // -- accessRestrictionsOnTheManifestation
        //  List<ManifestationAccessRestriction> accessRestrictions
        // not mapped

        // -- playingSpeed
        //  List<ManifestationPlayingSpeed> playingSpeeds
        manifMap.mapPlayingSpeeds(manif);

        // -- tapeConfiguration
        //  List<ManifestationTapeConfiguration> tapeConfigs
        manifMap.mapTapeConfigurations(manif);

        // -- kindOfSound
        //  List<ManifestationSoundKind> soundKinds
        manifMap.mapSoundKinds(manif);

        // -- specialReproductionCharacteristic
        //  List<ManifestationReproductionCharacteristic> reproChars
        manifMap.mapReproCharacteristics(manif);

        // -- fileCharacteristics
        //  List<ManifestationFileCharacteristic> fileChars
        // not mapped

        // -- modeOfAccess
        //  List<ManifestationAccessMode> accessModes
        // not manifMap.mapped in spec but coded
        manifMap.mapAccessModes(manif);

        // -- accessAddress
        //  List<ManifestationAccessAddress> accessAddresses
        manifMap.mapAccessAddresses(manif);

        // -- note
        //  List<ManifestationNote> notes
        manifMap.mapNotes(manif);

        // -- languageOfAccompanyingMaterials
        //  List<ManifestationAccompanyingLanguage> accompLangs
        manifMap.mapAccompLangs(manif);

        if (log.isInfoEnabled()) {
            log.info(" -- new manifestation titled: \""
                    + manif.getTitles().get(0).getText()
                    + "\"");
        }

        return manif;
    }
}
