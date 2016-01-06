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

import edu.indiana.dlib.vfrbr.persist.entity.attrib.AttribTypeNormal;
import edu.indiana.dlib.vfrbr.persist.entity.attrib.AttribTypeNormalFunction;

/**
 *
 * @author pbmcelwa
 */
public class DateNormalizer {

    /**
     *  Normalize a person |d subfield string
     * to AttribTypeNormalFunction attribute element fields.
     *
     * 2.1. Map balue from 100|d
     * 2.2. if 100|d included "yyyy-yyyy":
     *  2.2.1. Set value of @type to "range"
     *  2.2.3. Set value of @normal to "yyyy/yyyy"
     * 2.3. Else, set value of @type to "single":
     *  2.3.1. Set value of @normal to "yyyy"
     *  2.3.2. if |d includes "d.", set value of @function to "death"
     *  2.3.3. Else, set value of @function to "birth"
     *
     * @param dateField a person |d subfield string.
     * @param dateNormalized an AttribTypeNormalFunction attribute object.
     */
    public static void normalizePersonDate(
            final String dateField,
            final AttribTypeNormalFunction dateNormalized) {

        // 2.1 map value from subfield |d
        // -- text value set to whole subfield
        dateNormalized.setText(dateField);

        if (dateField.indexOf('-') >= 0) {
            // 2.2 if |d included "yyyy-yyyy"
            // 2.2.1 Set value of @type to "range"
            dateNormalized.setType("range");
            // 2.2.2 Set value of @normal to "yyyy/yyyy"
            dateNormalized.setNormal(dateField.replaceFirst("-", "/"));
        } else {
            // 2.3 Else, set value of @type to "single"
            dateNormalized.setType("single");
            // 2.3.1 Set value of @normal to "yyyy"
            // exclude non-integer characters from "yyyy"
            // -- first pass hack for "d." and "b."
            // -- now second pass for "d" and "b" at ending
            //      due to "cleaned" datefield
            if (dateField.trim().startsWith("d.")) {

                dateNormalized.setNormal(dateField.trim().substring(2));

            } else if (dateField.trim().endsWith("d")) {

                dateNormalized.setNormal(dateField.trim().substring(
                        0,
                        dateField.trim().length() - 2));

            } else if (dateField.trim().startsWith("b.")) {

                dateNormalized.setNormal(dateField.trim().substring(2));

            } else if (dateField.trim().endsWith("b")) {

                dateNormalized.setNormal(dateField.trim().substring(
                        0,
                        dateField.trim().length() - 2));

            } else {

                dateNormalized.setNormal(dateField);
            }

            if (dateField.indexOf("d.") >= 0) {
                // 2.3.2 if |d includes "d", set value of @function to "death"
                dateNormalized.setFunction("death");
            } else {
                // 2.3.3 Else, set value of @function to "birth"
                dateNormalized.setFunction("birth");
            }
        }
    }

    /**
     *  Normalize a corporateBody |d subfield string
     * to AttribTypeNormal attribute element fields.
     * (although CorporateBodyDate may extend AttribTypeNormalFunction,
     *  only the AttribTypeNormal extent is mapped)
     *
     *  4.1. Map value from 110/111, subfield |d
     *  4.2. If no |d existe at 110/111, map |d from 410/411
     *  4.3. If |d includes "yyyy-yyyy"
     *   4.3.1. Set value of @type to "range"
     *   4.3.2. Set value of @normal to "yyyy/yyyy"
     *  4.4. Else, set value of @type to "single"
     *   4.4.1. Set value of @normal to "yyyy"
     *
     * @param dateField a corporateBody |d subfield string.
     * @param dateNormalized an AttribTypeNormal attribute object.
     */
    public static void normalizeCorporateDate(
            final String dateField,
            final AttribTypeNormal dateNormalized) {

        // 4.1, 4.2 map value from subfield |d
        dateNormalized.setText(dateField);

        if (dateField.indexOf('-') >= 0) {
            // 4.3 if |d includes "yyyy-yyyy"
            // 4.3.1 set value of @type to "range"
            dateNormalized.setType("range");
            // 4.3.2 set value of @normal to "yyyy/yyyy"
            dateNormalized.setNormal(dateField.replaceFirst("-", "/"));
        } else {
            // 4.4 else, set value of @type to "single"
            dateNormalized.setType("single");
            // 4.4.1 set value of @normal to "yyyy"
            dateNormalized.setNormal(dateField);
        }
    }

    private static String baseDate;

    private static StringBuilder normalizing = new StringBuilder();

    /**
     * Normalize yyyymmdd to yyyy-mm-dd.
     * Replace any '-' date characters with '?'.
     *
     * @param dateField 033 |a
     * @return String of normalized form.
     */
    public static String normalizeExpr033Date(final String dateField) {
        // dateField: yyyymmdd...  (with '-' for unknown digits)
        // normalized: yyyy-mm-dd

        baseDate = dateField;
        normalizing.setLength(0);

        // replace any '-' with '?'
        baseDate = baseDate.replace('-', '?');

        // years

        if (baseDate.length() >= 4) {
            normalizing.append(baseDate.substring(0, 4));
        } else {
            normalizing.append(baseDate);
        }

        // months
        if (baseDate.length() == 5) {
            normalizing.append("-");
            normalizing.append(baseDate.substring(4, 5));
        } else if (baseDate.length() >= 6) {
            normalizing.append("-");
            normalizing.append(baseDate.substring(4, 6));
        }

        // days
        if (baseDate.length() == 7) {
            normalizing.append("-");
            normalizing.append(baseDate.substring(6, 7));
        } else if (baseDate.length() == 8) {
            normalizing.append("-");
            normalizing.append(baseDate.substring(6, 8));
        }

        return normalizing.toString();
    }
}
