/**
 * Copyright 2009-2011, Trustees of Indiana University
 * All rights reserved.
 * <p/>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p/>
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <p/>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <p/>
 * Neither the name of Indiana University nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p/>
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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.ManifestationMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import edu.indiana.dlib.vfrbr.persist.entity.manifestation.ManifestationJpa;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * Handle a MarcRecord.
 */
public class MarcRecordHandler {

    private static Logger log = Logger.getLogger(MarcRecordHandler.class);

    /**
     * Access to the persistence context.
     */
    private final DAOFactory daoFac;

    private final Counts count;

    /**
     * Instantiate a new MarcRecordHandler.
     *
     * @param daoFactory a DAOFactory holding the persistence context.
     */
    public MarcRecordHandler(DAOFactory daoFactory,
                             Counts count) {
        this.daoFac = daoFactory;
        this.count = count;
    }

    /**
     * Register handling a MarcRecord of type OTHER.
     *
     * @param marcRec
     */
    public final void registerOther(final MarcRecord marcRec) {

        /*
         * place holder for registration of OTHER type records
         * not processed
         */
    }

    /**
     * FRBRize a MarcRecord of type RECORDING.
     *
     * @param marcRec the MarcRecord to process.
     * @param count   counts to increment.
     */
    public final void frbrizeRecord(final MarcRecord marcRec) {

        ManifestationJpa marcRecManif;

        // how many works / manif
        int manifWorkCount = 0;

        try {

            responsiblePartiesPass(marcRec);

            marcRecManif = firstManifestationPass(marcRec);

            manifWorkCount = worksPass(marcRec, marcRecManif);

            finalManifestationPass(marcRec,
                    marcRecManif,
                    manifWorkCount);

        } catch (Exception ex) {
             ex.printStackTrace();
            // report exception and marcRecord
            log.error(
                    "\n********\n"
                            + "* Processing exception at ["
                            + count.getFileName() + ":" + count.getRecNum() + "]\n"
                            + "* attempting to continue processing with next record\n"
                            + "********",
                    ex);

        } finally {
            this.daoFac.flushClearEntityManager();
            this.daoFac.closeEntityManager();
        }
    }

    /**
     * Process the responsibleParties in a MarcRecord.
     *
     * @param marcRec the MarcRecord.
     * @param count   counts to increment.
     */
    private void responsiblePartiesPass(final MarcRecord marcRec) {

        log.info("==== Start of responsibleParties pass");

        final String[] personFieldTags = {
                "100", "600", "700"};
        final String[] corporateFieldTags = {
                "110", "111", "610", "611", "710", "711"};

        //get all contributor fields from the marcRecord
        final List<MarcDataField> personFields =
                marcRec.getDataFields(personFieldTags);
        final List<MarcDataField> corpBodyFields =
                marcRec.getDataFields(corporateFieldTags);

        //for each person
        final PersonFieldHandler persHandler = new PersonFieldHandler(this.daoFac, this.count);

        for (MarcDataField personField : personFields) {
            persHandler.handlePersonField(personField, marcRec.getControlNumber());
        }
        // for each corporateBody
        final CorporateFieldHandler corpHandler = new CorporateFieldHandler(this.daoFac, this.count);
        for (MarcDataField corpBodyField : corpBodyFields) {
            corpHandler.handleCorporateField(corpBodyField, marcRec.getControlNumber());
        }

        log.info("==== End of responsibleParties pass");
    }

    /**
     * Initialize the ManifestationJpa for this MarcRecord.
     *
     * @param marcRec the MarcRecord.
     * @return a new ManifestationJpa for this MarcRecord.
     */
    private ManifestationJpa firstManifestationPass(final MarcRecord marcRec) {

        log.info("==== Start of first manifestation pass");

        // set up the mapper
        final ManifestationMapper manifMapper =
                new ManifestationMapper(marcRec);

        // map new manifestation into existence
        final ManifestationJpa marcRecManif =
                manifMapper.getManifestation(this.daoFac.newManifestationDAO());

        log.info("==== End of first manifestation pass");

        return marcRecManif;
    }

    /**
     * Process the WorkJpa works in a MarcRecord.
     *
     * @param marcRec      the MarcRecord.
     * @param marcRecManif the ManifestationJpa for this MarcRecord.
     * @param count        counts to increment.
     */
    private int worksPass(final MarcRecord marcRec,
                          final ManifestationJpa marcRecManif) {

        log.info("==== Start of work pass");

        int workCount = 0;

        //find all work fields
        if (log.isInfoEnabled()) {
            log.info(" -- identifying workFields");
        }

        final List<WorkField> workFields =
                new WorkIdentification(marcRec).getAllWorkFields();

        if (log.isInfoEnabled()) {
            log.info("  "
                    + workFields.size()
                    + " workFields identified");
        }

        //process work fields
        final WorkFieldHandler workFieldHandler =
                new WorkFieldHandler(this.daoFac,
                        marcRec,
                        marcRecManif,
                        this.count);

        for (WorkField workField : workFields) {

            workFieldHandler.handleWorkField(workField);
            workCount++;
        }

        log.info("==== End of work pass for "
                + workCount + " works");

        return workCount;
    }

    /**
     * Process the ManifestationJpa manifestation for this MarcRecord.
     *
     * @param marcBibRec   the MarcRecord.
     * @param marcRecManif the ManifestationJpa manifestation.
     * @param count        counts to increment.
     */
    private void finalManifestationPass(final MarcRecord marcBibRec,
                                        final ManifestationJpa marcRecManif,
                                        int manifWorkCount) {

        log.info("==== Start of second manifestation pass");

        final ManifestationRecordHandler manifRecHandler =
                new ManifestationRecordHandler(this.daoFac,
                        this.count);

        manifRecHandler.handleManifestationRecord(marcBibRec,
                marcRecManif,
                manifWorkCount);

        log.info("==== End of second manifestation pass");
    }
}
