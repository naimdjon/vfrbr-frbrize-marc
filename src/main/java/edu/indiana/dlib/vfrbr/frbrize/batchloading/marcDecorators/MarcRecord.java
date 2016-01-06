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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

public class MarcRecord {

    public static final String GROUP1A = "GROUP1A";

    public static final String GROUP1B = "GROUP1B";

    public static final String GROUP1C = "GROUP1C";

    public static final String GROUP2 = "GROUP2";

    public static final String GROUP3 = "GROUP3";

    public static final String GROUP4 = "GROUP4";

    public static final String GROUP_ERROR = "GROUP ERROR";

    public static final String RECORDING = "Recording";

    public static final String SCORE = "Score";

    public static final String OTHER = "Other";

    private Record record;

    public MarcRecord(Record record) {
        this.record = record;
    }

    public boolean hasField(String field) {
        return (this.record.getVariableField(field) != null);
    }

    public boolean hasField(String[] fields) {
        return (this.getDataFields(fields).size() > 0);
    }

    public List<MarcDataField> getDataFields(String field) {
        List<?> tempFields = record.getVariableFields(field);
        List<MarcDataField> marcFields = new ArrayList<MarcDataField>();
        for (Object tempField : tempFields) {
            marcFields.add(new MarcDataField((DataField) tempField));
        }
        return marcFields;
    }

    public List<MarcDataField> getDataFields(String[] fields) {
        List<?> tempFields = record.getVariableFields(fields);
        List<MarcDataField> marcFields = new ArrayList<MarcDataField>();
        for (Object tempField : tempFields) {
            marcFields.add(new MarcDataField((DataField) tempField));
        }
        return marcFields;
    }

    public MarcDataField getDataField(String field) {
        DataField dataField = (DataField) record.getVariableField(field);
        if (null == dataField) {
            return null;
        } else {
            return new MarcDataField(dataField);
        }
    }

    public MarcControlField getControlField(String field) {
        ControlField controlField =
                (ControlField) record.getVariableField(field);
        if (null == controlField) {
            return null;
        } else {
            return new MarcControlField(controlField);
        }
    }

    public List<MarcControlField> getControlFields(String field) {
        List<?> tempFields = record.getVariableFields(field);
        List<MarcControlField> controlFields =
                new ArrayList<MarcControlField>();

        for (Object tempField : tempFields) {
            controlFields.add(new MarcControlField((ControlField) tempField));
        }

        return controlFields;
    }

    public String getControlNumber() {
        String tempCN = this.record.getControlNumber();
        if (tempCN == null) {
            return "";
        } else {
            return tempCN;
        }
    }

    /* This method is used to get the assigned group of a single MARC record based on
     * the following rules:
     *
     * Group1a - No 700|t ; 1xx/240/245
     * Group1b - No 700|t ; 1xx/245 (no 240)
     * Group1c - No 700|t ; 245 (no 1xx/240)
     * Group2 - Only 1 700|t
     * Group3 - 2 700|t
     * Group4 - 3+ 700|t
     */
    public String getGroup() {
        String[] fields7xx = {"700", "710", "711"};
        String[] fields1xx = {"100", "110", "111", "130"};
        String field240 = "240";
        String field245 = "245";

        //FIXED currently ignores subfield t 
//        int number7xx = (this.record.getVariableFields( fields7xx )).size();

        // of the 7xx fields, how many have 't' subfields
        int number7xxt = 0;
        Iterator<DataField> dataFields =
                (this.record.getVariableFields(fields7xx)).iterator();
        while (dataFields.hasNext()) {
            if (null != (dataFields.next()).getSubfield('t')) {
                number7xxt++;
            }
        }


        boolean has1xx;
        boolean has240;
        boolean has245;

        has1xx = (this.record.getVariableFields(fields1xx).size() > 0);
        has240 = (this.record.getVariableFields(field240).size() > 0);
        has245 = (this.record.getVariableFields(field245).size() > 0);

        if (number7xxt == 0) { //group1x check
            if (has1xx && has240 && has245) { //group1a check
                return GROUP1A;
            } else if (has1xx && has245) { //group1b check
                return GROUP1B;
            } else if (has245) { //group1c check
                return GROUP1C;
            } else { //error check
                //log.error("Could not assign record to a group"); //TODO add identifer
                return GROUP_ERROR;
            }
        } else if (number7xxt == 1) { //group2 check
            return GROUP2;
        } else if (number7xxt == 2) { //group3 check
            return GROUP3;
        } else { //group4
            return GROUP4;
        }
    }

    //leader position 6 values
    // j - Musical Recording
    public String getType() {
        Leader leader = this.record.getLeader();
        char typeChar = leader.getTypeOfRecord();
        switch (typeChar) {
            case 'j':
                return RECORDING;
            case 'c':
                return SCORE;
            case 'd':
                return SCORE;
            default:
                return OTHER;
        }
    }

    //leader position 6 value as char
    // for mappings beyond RECORDING or OTHER
    public char getTypeChar() {
        Leader leader = this.record.getLeader();

        return leader.getTypeOfRecord();
    }

    public Record getRecord() {
        return this.record;
    }

    @Override
    public String toString() {
        return this.record.toString();
    }
}