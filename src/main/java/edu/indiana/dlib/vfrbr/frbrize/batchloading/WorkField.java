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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;

/**
 *  Class to hold a Work MarcDataField and the workIdentAlgorithm string.
 * @author pbmcelwa
 */
public class WorkField {

    /**
     * An identified Work MarcDataField.
     */
    private MarcDataField workDataField;

    /**
     * String of the algorithm used for Work identification.
     */
    private String workIdentAlgorithm;

    /**
     * An identified Work MarcDataField.
     * @return the workDataField
     */
    public MarcDataField getWorkDataField() {
        return workDataField;
    }

    /**
     * An identified Work MarcDataField.
     * @param workDataField the workDataField to set
     */
    public void setWorkDataField(MarcDataField workDataField) {
        this.workDataField = workDataField;
    }

    /**
     * String of the algorithm used for Work identification.
     * @return the workIdentAlgorithm
     */
    public String getWorkIdentAlgorithm() {
        return workIdentAlgorithm;
    }

    /**
     * String of the algorithm used for Work identification.
     * @param workIdentAlgorithm the workIdentAlgorithm to set
     */
    public void setWorkIdentAlgorithm(String workIdentAlgorithm) {
        this.workIdentAlgorithm = workIdentAlgorithm;
    }

}
