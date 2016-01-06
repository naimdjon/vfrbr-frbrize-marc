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

import edu.indiana.dlib.vfrbr.frbrize.batchloading.mappers.WorkMapper;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcCollection;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcDataField;
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

public class Util {

    private static final String PREFIX = "/home/berry3/";
    private static final String[] MARC_FILES = {"data/mrc/frbr01.mrc",
        "data/mrc/frbr02.mrc",
        "data/mrc/frbr03.mrc",
        "data/mrc/frbr04.mrc",
        "data/mrc/frbr05.mrc",
        "data/mrc/frbr06.mrc",
        "data/mrc/frbr07.mrc",
        "data/mrc/frbr08.mrc",
        "data/mrc/frbr09.mrc",
        "data/mrc/frbr10.mrc",
        "data/mrc/frbr11.mrc",
        "data/mrc/frbr12.mrc",
        "data/mrc/frbr13.mrc",
        "data/mrc/frbr14.mrc",
        "data/mrc/frbr15.mrc",
        "data/mrc/frbr16.mrc",
        "data/mrc/frbr17.mrc",
        "data/mrc/frbr18.mrc",
        "data/mrc/frbr19.mrc",
        "data/mrc/frbr20.mrc"};
    
    private static String PEOPLE_OUT_FILE = "/home/amb/Desktop/people.txt";
    private static String CORP_OUT_FILE = "/home/amb/Desktop/corp.txt";
    private static String WORK_OUT_FILE = "/home/amb/Desktop/work.txt";


    public static void main(String args[]) {
        getListOfPeople();
        getListOfCorpBodies();
        getListOfWorks();
    }

    public static void getListOfPeople() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(PEOPLE_OUT_FILE, true));
            for(String file : MARC_FILES) {
                MarcCollection mc = new MarcCollection(PREFIX + file);
                while(mc.hasNext()) {
                    MarcRecord record = mc.next();
                    if(record.getType().equals(MarcRecord.RECORDING)) {
                        String[] peopleFields = {"100","600","700"};
                        List<MarcDataField> dfs = record.getDataFields(peopleFields);
                        for(MarcDataField df : dfs) {
                            String name = df.concatSubfields("abcdejq".toCharArray());
                            name = name.trim();
                            name = name.replaceAll(" +", " ");
                            name = Normalizer.normalize(name, Form.NFD);
                            name = name.replaceAll("[^\\p{ASCII}]", "");
                            name = name.toLowerCase();
                            out.append(name + "\n");
                        }
                    }
                }
            }
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void getListOfCorpBodies() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(CORP_OUT_FILE, true));
            for(String file : MARC_FILES) {
                MarcCollection mc = new MarcCollection(PREFIX + file);
                while(mc.hasNext()) {
                    MarcRecord record = mc.next();
                    if(record.getType().equals(MarcRecord.RECORDING)) {
                        String[] peopleFields = {"110","111","610", "611", "710", "711"};
                        List<MarcDataField> dfs = record.getDataFields(peopleFields);
                        for(MarcDataField df : dfs) {
                            String name = df.concatSubfields("abcdenq".toCharArray());
                            name = name.trim();
                            name = name.replaceAll(" +", " ");
                            name = Normalizer.normalize(name, Form.NFD);
                            name = name.replaceAll("[^\\p{ASCII}]", "");
                            name = name.toLowerCase();
                            out.append(name + "\n");
                        }
                    }
                }
            }
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void getListOfWorks() {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(WORK_OUT_FILE, true));
            for(String file : MARC_FILES) {
                MarcCollection mc = new MarcCollection(PREFIX + file);
                while(mc.hasNext()) {
                    MarcRecord record = mc.next();
                    if(record.getType().equals(MarcRecord.RECORDING)) {
                        WorkIdentification wi = new WorkIdentification(record);
                        List<WorkField> workFields = wi.getAllWorkFields();
                        for(WorkField workField : workFields) {
                            WorkMapper wm = new WorkMapper(workField.getWorkDataField(), record);
                            String name = wm.getComposer();
                            String title = wm.getUniformTitleString();

                            name = name.trim();
                            name = name.replaceAll(" +", " ");
                            name = Normalizer.normalize(name, Form.NFD);
                            name = name.replaceAll("[^\\p{ASCII}]", "");
                            name = name.toLowerCase();


                            title = title.trim();
                            title = title.replaceAll(" +", " ");

                            title = Normalizer.normalize(title, Form.NFD);
                            title = title.replaceAll("[^\\p{ASCII}]", "");
                            title = title.toLowerCase();
                            

                            out.append(title + "\t" + name + "\n");
                        }
                    }
                }
            }
            out.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
