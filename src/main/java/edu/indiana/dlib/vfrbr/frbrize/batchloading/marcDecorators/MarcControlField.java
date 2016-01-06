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
package edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators;

import org.marc4j.marc.ControlField;

/**
 * Decorator wrapper for Tag and Data of a Marc Record ControlField.
 *
 * No conversions.
 *
 * @author pbmcelwa
 */
public class MarcControlField {

    private ControlField controlField;

    /**
     * New MarcControlField wrapper for Marc Record ControlField.
     *
     * @param controlField the Marc Record ControlField.
     */
    public MarcControlField(ControlField controlField) {
        this.controlField = controlField;
    }

    /**
     * The tag String value of a control field.
     *
     * @return the String tag value.
     */
    public String getTag() {
        return this.controlField.getTag();
    }

    /**
     * The data value of a control field.
     *
     * @return String value of all the control field data.
     */
    public String getData() {
        return this.controlField.getData();
    }

    /**
     * Does the ControlField Data include the range extent?
     *
     * Zero based indexing, inclusive range positions.
     *
     * @param rangeStart int of range extent beginning.
     * @param rangeEnd int of range extent ending.
     * @return true/false of whether ControlField Data includes the range extent.
     */
    public boolean hasRange(int rangeStart, int rangeEnd) {
        return (this.controlField.getData().length() >= (rangeEnd + 1));
    }

    /**
     * Get the range extent from the ControlField Data.
     *
     * Zero based indexing, inclusive range positions.
     *
     * @param rangeStart int of range extent beginning.
     * @param rangeEnd int of range extent ending.
     * @return String substring of the range extent of the ControlField Data.
     */
    public String getRange(int rangeStart, int rangeEnd) {
        return (this.controlField.getData().substring(rangeStart, rangeEnd + 1));
    }

    @Override
    public String toString() {
        return this.controlField.toString();
    }

}
