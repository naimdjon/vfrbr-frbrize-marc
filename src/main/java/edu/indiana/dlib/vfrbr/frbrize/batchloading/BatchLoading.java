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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcCollection;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import edu.indiana.dlib.vfrbr.persist.dao.DAOFactory;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.google.common.collect.Sets.newHashSet;
import static edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord.OTHER;
import static edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord.RECORDING;
import static edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord.SCORE;

/**
 * FRBRize MARC bibliographic marcRec files to database persistence.
 * Referenced MARC files will be batch processed to Java JPA annotated classes
 * and persisted.
 */
public final class BatchLoading {

    private static final Logger log = Logger.getLogger(BatchLoading.class);

    private String PREFIX = null;

    // TODO externalize the data file list to be outside the compiled code
    private final String[] MARC_FILES = {
            "frbr01.mrc",
            "frbr02.mrc",
            "frbr03.mrc",
            "frbr04.mrc",
            "frbr05.mrc",
            "frbr06.mrc",
            "frbr07.mrc",
            "frbr08.mrc",
            "frbr09.mrc",
            "frbr10.mrc",
            "frbr11.mrc",
            "frbr12.mrc",
            "frbr13.mrc",
            "frbr14.mrc",
            "frbr15.mrc",
            "frbr16.mrc",
            "frbr17.mrc",
            "frbr18.mrc",
            "frbr19.mrc",
            "frbr20.mrc"
    };

    public static final String PERSON_ATTRIBUTE = "Person";

    public static final String GROUP_ATTRIBUTE = "Group";

    // TODO fix WorkIdentification to use titles black list properties
    protected static final String[] COLLECTIVE_TITLES_BLACK_LIST = {
            "Chamber music",
            "Choral music",
            "Electronic music",
            "Harpsichord music",
            "Instrumental music",
            "Lute music",
            "Keyboard music",
            "Musicals",
            "Orchestra music",
            "Organ music",
            "Piano music",
            "Selections",
            "String quartet music",
            "Violin, harpsichord music",
            "Violin, piano music",
            "Violoncello, piano music",
            "Vocal music",
            "Works"};

    // TODO fix WorkIdentification to use forms properties instead of this list
    protected static final String[] FORMS_LIST = {
            "Adagios",
            "Allegros",
            "Allemandes",
            "Anthems",
            "Arias",
            "Bagatelles",
            "Ballades",
            "Berceuses",
            "Canons",
            "Canzonas",
            "Canzonettas",
            "Caprices",
            "Cappricios",
            "Cassations",
            "Choruses",
            "Concertinos",
            "Concertos",
            "Divertimenti", //*
            "Divertimentos",
            "Duets",
            "Elegies",
            "Etudes",
            "Fanfares",
            "Fantasias",
            "Fugues",
            "Gavottes",
            "Gigues",
            "Hymns",
            "Intermezzi", //*
            "Intermezzos",
            "Largos",
            "Lieder", //*
            "Marches",
            "Melodies",
            "Minuets",
            "Nocturnes",
            "Nonets",
            "Octets",
            "Odes",
            "Partitas",
            "Pavans",
            "Pieces",
            "Poems",
            "Polkas",
            "Polonaises",
            "Preludes",
            "Psalms",
            "Quartets",
            "Quintets",
            "Rhapsodies",
            "Romances",
            "Rondos",
            "Scherzos",
            "Septets",
            "Sextets",
            "Sonatas",
            "Sontinas",
            "Songs",
            "Studies",
            "Suites",
            "Toccatas",
            "Trio sonatas",
            "Trios",
            "Variations",
            "Waltzes"};

    /**
     * Main program entrance point.
     * No argument expected or processed.
     * Batchloading is intitiated via constructor.
     *
     * @param args arguments.
     */
    public static void main(final String[] args) {

        // instantiate to process
        new BatchLoading();

    }

    /**
     * Constructor that initializes and then invokes batchloading.
     */
    private BatchLoading() {
        try {
            /*
             * get the batchLoading properties
             * for persistence unit name and MARC daata files location
             */
            final ClassLoader loader =
                    Thread.currentThread().getContextClassLoader();
            final InputStream inStream =
                    loader.getResourceAsStream("batchLoading.properties");
            if (inStream == null) {
                log.error("==*!!*== Can't load batchLoading.properties.");
                throw new IllegalStateException("Can't load batchLoading properties");
            }

            try {
                final Properties batchLoadingProps = new Properties();
                batchLoadingProps.load(inStream);
                PREFIX =
                        batchLoadingProps.getProperty("marc_data_path").replaceFirst("^~", System.getProperty("user.home"));
            } catch (IOException ex) {
                log.error("==*!!*== load failed for authCache.properties.");
                throw ex;
            }


            this.startBatchloading();

        } catch (Exception e) {
            log.error("Error in batchloading process.", e);
        }
    }

    /**
     * Batchload the array of file names.
     */
    private void startBatchloading() {
        // TODO report batchLoadingProps settings ???

        DAOFactory daoFac = null;
        final Counts accumulatedCounts = new Counts();
        Counts count = null;

        try {
            log.warn("======= Starting BatchLoad process: \n"
                    + ", data_path: " + PREFIX);
            System.out.println("location:" + getClass().getProtectionDomain().getCodeSource().getLocation());
            System.out.println("======= Starting BatchLoad process: \n"
                    + ", data_path: " + PREFIX);

            for (String currentFile : getMarcFiles()) {
                /*
                 * ==> a(nother) MARC data file
                 */
                // new DAOFactory for each MARC data file
                daoFac = new DAOFactory();

                count = new Counts();
                count.setFileName(currentFile);

                final String currentPath = PREFIX + currentFile;

                log.warn(" ");
                log.warn("======= Starting file " + currentFile
                        + " =======");

                System.out.println("======= Starting file " + currentFile
                        + " =======");

                final MarcCollection marcRecs =
                        new MarcCollection(currentPath);

                while (marcRecs.hasNext()) {
                    /*
                     * ==> a(nother) MARC Record
                     */
                    count.incrementRecNum();
                    final MarcRecord marcRec = marcRecs.next();
                    final MarcRecordHandler recHandler = new MarcRecordHandler(daoFac, count);

                    if (newHashSet(RECORDING, SCORE, OTHER).contains(marcRec.getType())) {
                        /*
                         * ==> a MARC Record of type RECORDING or SCORE
                         */
                        log.info(" ");
                        log.info("===== "
                                + marcRec.getType()
                                + " Type Record ["
                                + count.getFileName() + ":" + count.getRecNum()
                                + "]"
                                + " =====");
                        log.info("---- MARC Bib record:\n"
                                + marcRec.toString()
                                + "----");

                        recHandler.frbrizeRecord(marcRec);

                    } else {
                        /*
                         * ==> a MARC Record of type OTHER
                         */
                        log.info(" ");
                        log.info("===== OTHER Type Record ["
                                + count.getFileName() + ":" + count.getRecNum()
                                + "]"
                                + " =====");
                        log.info("---- MARC Bib record:\n"
                                + marcRec.toString()
                                + "----");

                        recHandler.registerOther(marcRec);
                    }

                } // of all the records in this MARC data file

                // accumulate running counts
                accumulatedCounts.accumulate(count);
                // report file counts
                log.warn(count.reportCurrentFileCounts());

                // cycle the DAOFactory at the end of the file
                daoFac.closeEntityManager();
                daoFac.getEntityManagerFactory().close();

            } // of all the MARC data files to process


            log.warn("======= BatchLoad process complete.");

            System.out.println("======= BatchLoad process complete.");

        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            log.error("\n"
                    + "============\n"
                    + " At counts for file: " + count.getFileName() + "\n"
                    + "   records procesed:        "
                    + count.getRecNum() + "\n"
                    + "============\n");
        } finally {

            // report total counts
            log.warn(accumulatedCounts.reportAccumulatedFileCounts());

            System.out.println(accumulatedCounts.reportAccumulatedFileCounts());

        }
    }

    private String[] getMarcFiles() {
        for (final String file : MARC_FILES) {
            if (new File(PREFIX + file).exists()) {
                System.out.println("exists...");
                return MARC_FILES;
            }
        }
        log.info("None of the default MARC_FILES exist. Returning listing of files from the batchloading marc_data_path");
        final File marc_data_path = new File(PREFIX);
        if (marc_data_path.exists()) {
            return marc_data_path.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".mrc");
                }
            });
        }
        return this.MARC_FILES;
    }
}
