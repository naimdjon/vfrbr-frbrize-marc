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

import java.util.HashMap;
import java.util.HashSet;

/**
 *  Utility class for servicing information about relator codes for roles.
 *
 * @author pbmcelwa
 */
public class Roles {

    // --- internal data structures
    /**
     *  Map of [relator code] => [relator label].
     */
    private static final HashMap<String, String> codeMap =
            new HashMap<String, String>();

    /**
     *  Set of creator relator codes.
     * Roles  for Work2ResponsibleParty relations.
     */
    private static final HashSet<String> creatorCodes =
            new HashSet<String>();

    /**
     *  Set of realizer relator codes.
     * Roles for Expression2ResponsibleParty relations.
     */
    private static final HashSet<String> realizerCodes =
            new HashSet<String>();

    /**
     *  Set of producer relator codes.
     * Roles for Manifestation2ResponsibleParty relations.
     */
    private static final HashSet<String> producerCodes =
            new HashSet<String>();

    /*
     * Initialization of internal data structures
     */
    static {
        /*
         *  relator codes
         * from: http://www.loc.gov/marc/relators/relaterm.html
         */
        // arranger
        codeMap.put("arr", "arranger");
        // choreographer
        codeMap.put("chr", "choreographer");
        // composer
        codeMap.put("cmp", "composer");
        // conductor
        codeMap.put("cnd", "conductor");
        // editor
        codeMap.put("edt", "editor");
        // illustrator
        codeMap.put("ill", "illustrator");
        // instrumentalist
        codeMap.put("itr", "instrumentalist");
        // librettist
        codeMap.put("lbt", "librettist");
        // lithographer
        codeMap.put("ltg", "lithographer");
        // lyricist
        codeMap.put("lyr", "lyricist");
        // musician
        codeMap.put("mus", "musician");
        // performer
        codeMap.put("prf", "performer");
        // recordingEngineer
        codeMap.put("rce", "recordingEngineer");
        // translator
        codeMap.put("trl", "translator");
        // vocalist
        codeMap.put("voc", "vocalist");

        /*
         * creator roles
         */
        // composer
        creatorCodes.add("cmp");
        // librettist
        creatorCodes.add("lbt");
        // lyricist
        creatorCodes.add("lyr");

        /*
         * realizer roles
         */
        // arranger
        realizerCodes.add("arr");
        // choreographer
        realizerCodes.add("chr");
        // conductor
        realizerCodes.add("cnd");
        // instrumentalist
        realizerCodes.add("itr");
        // musician
        realizerCodes.add("mus");
        // performer
        realizerCodes.add("prf");
        // translator
        realizerCodes.add("trl");
        // vocalist
        realizerCodes.add("voc");

        /*
         * producer roles
         */
        // editor
        producerCodes.add("edt");
        // illustrator
        producerCodes.add("ill");
        // lithographer
        producerCodes.add("ltg");
        // recordingEngineer
        producerCodes.add("rce");

    }

    /**
     * Not meant to be instantiated or persisted
     */
    private Roles() {
    }
    /*
     * ---- public services
     */

    /**
     *  Get the name label for a relator code.
     * @param code String relator code.
     * @return String label name for the relator code.
     */
    public static final String getRelatorCodeName(String code) {

        return codeMap.get(code);
    }

    /**
     *  Is the relator code a creator role.
     * (Work to ResponsibleParty)
     * @param code String relator code.
     * @return boolean of relator code being a creator role.
     */
    public static final boolean isCreatorRoleCode(String code) {

        return creatorCodes.contains(code);
    }

    /**
     *  Is the relator code a realizer role.
     * (Expression to ResponsibleParty)
     * @param code String relator code.
     * @return boolean of relator code being a realizer role.
     */
    public static final boolean isRealizerRoleCode(String code) {

        return realizerCodes.contains(code);
    }

    /**
     *  Is the relator code a producer role.
     * (Manifestation to ResponsibleParty)
     * @param code String relator code.
     * @return boolean of relator code being a producer role.
     */
    public static final boolean isProducerRoleCode(String code) {

        return producerCodes.contains(code);
    }

    /**
     *  Is the relator code a recognized role.
     * @param code String relator code.
     * @return boolean of relator code being a recognized role.
     */
    public static final boolean isRecognizedRoleCode(String code) {

        return codeMap.containsKey(code);
    }
}
