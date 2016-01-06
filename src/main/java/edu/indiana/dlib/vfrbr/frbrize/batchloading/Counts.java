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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.indiana.dlib.vfrbr.frbrize.batchloading;

/**
 *
 * @author pbmcelwa
 */
public class Counts {

    /*
     * ------ local properties
     */
    /**
     * Newly persisted Persons.
     */
    protected int persistedPersons;

    /**
     * Newly persisted CorporateBodies.
     */
    protected int persistedCorps;

    /**
     * Newly persisted Works.
     */
    protected int persistedWorks;

    /**
     * Newly persisted Expressions.
     */
    protected int persistedExpressions;

    /**
     * Newly persisted Manifestations.
     */
    protected int persistedManifestations;

    /**
     * Unmatched composer references.
     */
    protected int unmatchedComposers;

    /**
     * Unmatched creator references.
     */
    protected int unmatchedCreators;

    /**
     * Unmatched realizer references.
     */
    protected int unmatchedRealizers;

    /**
     * Unmatched producer references.
     */
    protected int unmatchedProducers;

    /**
     * Current MARC file record number.
     */
    protected int recNum;

    /**
     * Accumulated MARC file records processed.
     */
    protected int recNums;

    /**
     * Current MARC file name;
     */
    protected String fileName;

    /**
     * Accumulated MARC file names and record counts.
     */
    protected String fileNames;

    /*
     * ---- incrementors for local counts
     */
    public void incrementPersistedPersons() {
        this.persistedPersons++;
    }

    public void incrementPersistedCorporateBodies() {
        this.persistedCorps++;
    }

    public void incrementPersistedWorks() {
        this.persistedWorks++;
    }

    public void incrementPersistedExpressions() {
        this.persistedExpressions++;
    }

    public void incrementPersistedManifestations() {
        this.persistedManifestations++;
    }

    public void incrementUnmatchedComposers() {
        this.unmatchedComposers++;
    }

    public void incrementUnmatchedCreators() {
        this.unmatchedCreators++;
    }

    public void incrementUnmatchedRealizers() {
        this.unmatchedRealizers++;
    }

    public void incrementUnmatchedProducers() {
        this.unmatchedProducers++;
    }

    public void incrementRecNum() {
        this.recNum++;
    }

    /**
     * Report counts for current fileName
     */
    public String reportCurrentFileCounts() {
        StringBuilder strBuff = new StringBuilder();

        strBuff.append("\n============\n");
        strBuff.append(" Counts for file: ");
        strBuff.append(this.fileName);
        strBuff.append("\n   records procesed:        ");
        strBuff.append(this.recNum);
        strBuff.append("\n   new persisted persons: ");
        strBuff.append(this.persistedPersons);
        strBuff.append("\n   new persisted corporations: ");
        strBuff.append(this.persistedCorps);
        strBuff.append("\n   new persisted works:      ");
        strBuff.append(this.persistedWorks);
        strBuff.append("\n   new persisted expressions:         ");
        strBuff.append(this.persistedExpressions);
        strBuff.append("\n   new persisted manifestations:      ");
        strBuff.append(this.persistedManifestations);
        strBuff.append("\n  ---");
        strBuff.append("\n   unmatched composers:        ");
        strBuff.append(this.unmatchedComposers);
        strBuff.append("\n   unmatched creators:       ");
        strBuff.append(this.unmatchedCreators);
        strBuff.append("\n   unmatched realizers:       ");
        strBuff.append(this.unmatchedRealizers);
        strBuff.append("\n   unmatched producers:       ");
        strBuff.append(this.unmatchedProducers);
        strBuff.append("\n============\n");

        return strBuff.toString();
    }

    /**
     * Report counts for accumulated fileNames
     */
    public String reportAccumulatedFileCounts() {
        StringBuilder strBuff = new StringBuilder();

        strBuff.append("\n============\n");
        strBuff.append(" Counts for files: ");
        strBuff.append(this.fileNames);
        strBuff.append("\n   new persisted persons: ");
        strBuff.append(this.persistedPersons);
        strBuff.append("\n   new persisted corporations: ");
        strBuff.append(this.persistedCorps);
        strBuff.append("\n   new persisted works:      ");
        strBuff.append(this.persistedWorks);
        strBuff.append("\n   new persisted expressions:         ");
        strBuff.append(this.persistedExpressions);
        strBuff.append("\n   new persisted manifestations:      ");
        strBuff.append(this.persistedManifestations);
        strBuff.append("\n  ---");
        strBuff.append("\n   unmatched composers:        ");
        strBuff.append(this.unmatchedComposers);
        strBuff.append("\n   unmatched creators:       ");
        strBuff.append(this.unmatchedCreators);
        strBuff.append("\n   unmatched realizers:       ");
        strBuff.append(this.unmatchedRealizers);
        strBuff.append("\n   unmatched producers:       ");
        strBuff.append(this.unmatchedProducers);
        strBuff.append("\n============\n");

        return strBuff.toString();
    }

    /**
     * Accumulate across count instances.
     * For accumulating counts into an accumulator count.
     * @param count Count instance to accumulate.
     */
    public void accumulate(Counts count) {
        // persistences
        this.persistedPersons += count.persistedPersons;
        this.persistedCorps += count.persistedCorps;
        this.persistedWorks += count.persistedWorks;
        this.persistedExpressions += count.persistedExpressions;
        this.persistedManifestations += count.persistedManifestations;

        // non-matches
        this.unmatchedComposers += count.unmatchedComposers;
        this.unmatchedCreators += count.unmatchedCreators;
        this.unmatchedRealizers += count.unmatchedRealizers;
        this.unmatchedProducers += count.unmatchedProducers;

        // records
        this.recNums += count.recNum;

        // files
        if (null == this.fileNames) {
            // initial accumulation
            this.fileNames =
                    count.getFileName()
                    + "[" + count.getRecNum() + "]";
        } else {
            // succesive accumulations
            this.fileNames =
                    this.fileNames
                    + ",\n                   "
                    + count.getFileName()
                    + "[" + count.getRecNum() + "]";
        }
    }

    /**
     * @return the recNum
     */
    public int getRecNum() {
        return recNum;
    }

    /**
     * @param recNum the recNum to set
     */
    public void setRecNum(int recNum) {
        this.recNum = recNum;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
