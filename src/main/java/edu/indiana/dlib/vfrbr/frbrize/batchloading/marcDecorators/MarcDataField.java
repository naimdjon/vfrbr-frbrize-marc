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
import java.util.List;

import org.marc4j.converter.impl.AnselToUnicode;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Subfield;

public class MarcDataField {

    DataField dataField;

    public MarcDataField(DataField variableField) {
        this.dataField = variableField;
    }

    public String getTag() {
        return this.dataField.getTag();
    }

    public boolean hasSubfields(char[] fields) {
        return !(this.concatSubfields(fields).equals(""));
    }

    public String get1stIndicator() {
        String returnValue = String.valueOf(this.dataField.getIndicator1());
        AnselToUnicode converter = new AnselToUnicode();
        return converter.convert(returnValue);
    }

    public String get2ndIndicator() {
        String returnValue = String.valueOf(this.dataField.getIndicator2());
        AnselToUnicode converter = new AnselToUnicode();
        return converter.convert(returnValue);
    }

    public String concatSubfields(char[] fields) {
        String returnValue = "";
        for (char currentField : fields) {
            List<Subfield> values = dataField.getSubfields(currentField);
            for (Subfield value : values) {
                returnValue = returnValue + " " + value.getData();
            }
        }

        AnselToUnicode converter = new AnselToUnicode();

        return clean(converter.convert(returnValue));
    }

    public String concatAllBut(String excludeFields) {
        String returnValue = "";
        for (Object tempObject : this.dataField.getSubfields()) {
            Subfield currentSubfield = (Subfield) tempObject;
            if (!excludeFields.contains(
                    String.valueOf(currentSubfield.getCode()))) {
                returnValue = returnValue + " " + currentSubfield.getData();
            }
        }

        AnselToUnicode converter = new AnselToUnicode();
        return clean(converter.convert(returnValue));
    }

    public List<String> getValueList(char field) {
        ArrayList<String> returnList = new ArrayList<String>();

        List<Subfield> tempList = this.dataField.getSubfields(field);

        AnselToUnicode converter = new AnselToUnicode();

        for (Subfield sf : tempList) {
            if (sf != null) {
                returnList.add(clean(converter.convert(sf.getData())));
            }
        }

        return returnList;
    }

    public String getValue(char field) {
        String returnValue = null;

        Subfield subField = this.dataField.getSubfield(field);
        if (subField != null) {
            AnselToUnicode converter = new AnselToUnicode();
            returnValue = clean(converter.convert(subField.getData()));
        }

        return returnValue;
    }

    private String clean(String value) {
        String trimmedValue = value.trim();
        if (trimmedValue.endsWith(".")
                || trimmedValue.endsWith(",")
                || trimmedValue.endsWith(";")
                || trimmedValue.endsWith(";")) {
            return trimmedValue.substring(0, trimmedValue.length() - 1);
        } else {
            return trimmedValue;
        }
    }
    //TODO maybe implement?
    //public String concatAllSubfields() {
    //	dataField.
    //}

    @Override
    public String toString() {
        return this.dataField.toString();
    }
}
