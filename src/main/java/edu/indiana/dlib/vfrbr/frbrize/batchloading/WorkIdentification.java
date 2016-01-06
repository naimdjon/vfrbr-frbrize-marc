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
import edu.indiana.dlib.vfrbr.frbrize.batchloading.marcDecorators.MarcRecord;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *  Identification of work fields and distinction of work groups.
 *
 */
public class WorkIdentification {

    private static Logger log = Logger.getLogger(WorkIdentification.class);

    final private MarcRecord marcRec;

    final private List<WorkField> workFields;

    /**
     *  Instantiate and initialize a WorkIdentification.
     *
     * @param marcRecord the MarcRecord being processed for works.
     */
    public WorkIdentification(final MarcRecord marcRecord) {
        this.marcRec = marcRecord;
        this.workFields = new ArrayList<WorkField>();
    }

    /**
     *  Get all the work fields from a MarcRecord.
     *
     * @return List of MarcDataField work fields.
     */
    public final List<WorkField> getAllWorkFields() {

        // process the MarcRecord by group type
        final String recordGroup = this.marcRec.getGroup();

        if (recordGroup.equals(MarcRecord.GROUP1A)) {
            handleGroup1a();
        } else if (recordGroup.equals(MarcRecord.GROUP1B)) {
            handleGroup1b();
        } else if (recordGroup.equals(MarcRecord.GROUP1C)) {
            //do nothing
        } else if (recordGroup.equals(MarcRecord.GROUP2)) {
            handleGroup2();
        } else if (recordGroup.equals(MarcRecord.GROUP3)) {
            handleGroup3();
        } else if (recordGroup.equals(MarcRecord.GROUP4)) {
            handleGroup4();
        }

        return workFields;
    }

    private void handleGroup1a() {

        MarcDataField field240 = this.marcRec.getDataField("240");
        char[] a = {'a'};
        char[] m = {'m'};
        char[] npr = {'n', 'p', 'r'};
        char[] mnpr = {'m', 'n', 'p', 'r'};
        String value240a = field240.concatSubfields(a);

        for (String collectiveTitle :
                BatchLoading.COLLECTIVE_TITLES_BLACK_LIST) {

            if (value240a.trim().toLowerCase().equals(
                    collectiveTitle.toLowerCase())) {

                return; //Not work (1.1.1)
            }
        }

        for (String form : BatchLoading.FORMS_LIST) {

            if (value240a.trim().toLowerCase().equals(form.toLowerCase())) {
                // (1.1.2)

                if (!field240.hasSubfields(mnpr)) {

                    return; //Not work (1.1.2.1)

                } else if (field240.hasSubfields(m)
                        && !field240.hasSubfields(npr)) {

                    WorkField workField = new WorkField();
                    workField.setWorkDataField(field240);
                    workField.setWorkIdentAlgorithm("1.1.2.2");
                    this.workFields.add(workField);
                    if (log.isInfoEnabled()) {
                        log.info("  identified group1a 1.1.2.2: "
                                + field240.toString());
                    }
                    return; //Work (1.1.2.2)

                } else {

                    WorkField workField = new WorkField();
                    workField.setWorkDataField(field240);
                    workField.setWorkIdentAlgorithm("1.1.2.3");
                    this.workFields.add(workField);
                    if (log.isInfoEnabled()) {
                        log.info("  identifed group1a 1.1.2.3: "
                                + field240.toString());
                    }
                    return; //Work.
                }
            }
        }

        WorkField workField = new WorkField();
        workField.setWorkDataField(field240);
        workField.setWorkIdentAlgorithm("1.1.3");
        this.workFields.add(workField);
        if (log.isInfoEnabled()) {
            log.info("  identified group1a 1.1.3: "
                    + field240.toString());
        }
        return;//Work.
    }

    private void handleGroup1b() {

        if (this.marcRec.hasField("100")) {
            // (2.1)

            handle245Group1b();
            handle740Group1b("100");

        } else if (this.marcRec.hasField("110")) {
            // (2.2)

            handle740Group1b("110");

        } else if (this.marcRec.hasField("111")) {
            // (2.3)

            handle740Group1b("111");

        } else if (this.marcRec.hasField("130")) {
            // (2.4)

            WorkField workField = new WorkField();
            workField.setWorkDataField(this.marcRec.getDataField("130"));
            workField.setWorkIdentAlgorithm("2.4.1");
            this.workFields.add(workField);
            if (log.isInfoEnabled()) {
                log.info("  identified group1b 2.4.1: "
                        + this.marcRec.getDataField("130").toString());
            }
        }
    }

    private void handle245Group1b() {

        if (!this.marcRec.getDataField("100").hasSubfields("4".toCharArray())
                || this.marcRec.getDataField("100").
                concatSubfields("4".toCharArray()).equals("cmp")) {

            if (this.marcRec.hasField("740")) {
                // (2.1.2.1.1)

                WorkField workField = new WorkField();
                workField.setWorkDataField(marcRec.getDataField("245"));
                workField.setWorkIdentAlgorithm("2.1.2.1.1");
                this.workFields.add(workField);
                if (log.isInfoEnabled()) {
                    log.info("  identified 245 group1b 2.1.2.1.1: "
                            + marcRec.getDataField("245").toString());
                }

            } else if (!this.marcRec.hasField("505")) {
                // (2.1.2.2.2)

                WorkField workField = new WorkField();
                workField.setWorkDataField(marcRec.getDataField("245"));
                workField.setWorkIdentAlgorithm("2.1.2.2.2");
                this.workFields.add(workField);
                if (log.isInfoEnabled()) {
                    log.info("  identified 245 group1b 2.1.2.2.2: "
                            + marcRec.getDataField("245").toString());
                }

            }
        }
    }

    private void handle740Group1b(String tag) {

        List<MarcDataField> field740s = this.marcRec.getDataFields("740");

        for (MarcDataField current740 : field740s) {

            if (current740.get2ndIndicator().equals("2")) {

                WorkField workField = new WorkField();
                workField.setWorkDataField(current740);
                if ("100".equals(tag)) {
                    workField.setWorkIdentAlgorithm("2.1.3.1");
                } else if ("110".equals(tag)) {
                    workField.setWorkIdentAlgorithm("2.2.2.1");
                } else if ("111".equals(tag)) {
                    workField.setWorkIdentAlgorithm("2.3.2.1");
                }
                this.workFields.add(workField);
                if (log.isInfoEnabled()) {
                    log.info("  identified 740 group1b "
                            + workField.getWorkIdentAlgorithm() + ": "
                            + current740.toString());
                }
            }
        }
    }

    private void handleGroup2() {

        handle240Group2("2");
        handle245Group2();
        handleAll700sGroup2("2");
    }

    private void handleGroup3() {

        handle240Group2("3");
        handleAll700sGroup2("3");
    }

    private void handleGroup4() {

        handle240Group2("4");
        handleAll700sGroup2("4");
    }

    private void handle240Group2(String group) {

        // (4.1)
        if (!this.marcRec.hasField("240")) {
            return;
        }

        MarcDataField field240 = this.marcRec.getDataField("240");
        char[] a = {'a'};
        char[] m = {'m'};
        char[] npr = {'n', 'p', 'r'};
        char[] mnpr = {'m', 'n', 'p', 'r'};
        String value240a = field240.concatSubfields(a);

        // (4.1.1)
        for (String collectiveTitle :
                BatchLoading.COLLECTIVE_TITLES_BLACK_LIST) {

            if (value240a.trim().toLowerCase().equals(
                    collectiveTitle.toLowerCase())) {

                return;
            }
        }

        // (4.1.2)
        for (String form : BatchLoading.FORMS_LIST) {

            if (value240a.trim().toLowerCase().equals(form.toLowerCase())) {

                // (4.1.2.1)
                if (!field240.hasSubfields(mnpr)) {

                    return;


                } else if (field240.hasSubfields(m)
                        && !field240.hasSubfields(npr)) {
                    // (4.1.2.2)
                    return;

                } else if (field240.hasSubfields(npr)) {

                    WorkField workField = new WorkField();
                    workField.setWorkDataField(field240);
                    if ("2".equals(group)) {
                        workField.setWorkIdentAlgorithm("4.1.2.3");
                    } else if ("3".equals(group)) {
                        workField.setWorkIdentAlgorithm("5.1.2.3");
                    } else if ("4".equals(group)) {
                        workField.setWorkIdentAlgorithm("6.1.2.3");
                    }
                    this.workFields.add(workField);
                    if (log.isInfoEnabled()) {
                        log.info("  identified 240 group2 "
                                + workField.getWorkIdentAlgorithm() + ": "
                                + field240.toString());
                    }
                    return;

                }
            }
        }

        // did not match a value from the Forms list

        WorkField workField = new WorkField();
        workField.setWorkDataField(field240);
        if ("2".equals(group)) {
            workField.setWorkIdentAlgorithm("4.1.3");
        } else if ("3".equals(group)) {
            workField.setWorkIdentAlgorithm("5.1.3");
        } else if ("4".equals(group)) {
            workField.setWorkIdentAlgorithm("6.1.3");
        }
        this.workFields.add(workField);
        if (log.isInfoEnabled()) {
            log.info("  identified 240 group2 "
                    + workField.getWorkIdentAlgorithm() + ": "
                    + field240.toString());
        }
        return;
    }

    private void handle245Group2() {

        char[] subfield4 = {'4'};

        if (this.marcRec.hasField("240")) {
            return;
        }
        if (!this.marcRec.hasField("245")) {
            return;
        }

        MarcDataField field245 = this.marcRec.getDataField("245");

        if (!this.marcRec.hasField("100")) {
            return;
        }

        MarcDataField field100 = this.marcRec.getDataField("100");

        if (!field100.hasSubfields(subfield4)
                || field100.concatSubfields(subfield4).trim().equals("cmp")
                || field100.concatSubfields(subfield4).trim().equals("lbt")
                || field100.concatSubfields(subfield4).trim().equals("lyr")) {

            WorkField workField = new WorkField();
            workField.setWorkDataField(field245);
            workField.setWorkIdentAlgorithm("4.2.1.1.1");
            this.workFields.add(workField);
            if (log.isInfoEnabled()) {
                log.info("  identified 245 group2 "
                        + workField.getWorkIdentAlgorithm() + ": "
                        + workField.getWorkDataField().toString());
            }
            return;

        } else if (this.marcRec.hasField("110")
                || this.marcRec.hasField("111")) {

            //do nothing
            return;
        } else if (this.marcRec.hasField("130")) {

            WorkField workField = new WorkField();
            workField.setWorkDataField(this.marcRec.getDataField("130"));
            workField.setWorkIdentAlgorithm("4.2.1.3");
            this.workFields.add(workField);
            if (log.isInfoEnabled()) {
                log.info("  identified 245 group2 "
                        + workField.getWorkIdentAlgorithm() + ": "
                        + workField.getWorkDataField().toString());
            }
        }
    }

    private void handleAll700sGroup2(String group) {
        List<MarcDataField> all700Fields = this.marcRec.getDataFields("700");

        for (MarcDataField current700Field : all700Fields) {

            handleSingle700Group2(current700Field, group);
        }
    }

    private void handleSingle700Group2(MarcDataField field700,
                                       String group) {
        char[] t = {'t'};
        char[] m = {'m'};
        char[] k = {'k'};
        char[] npr = {'n', 'p', 'r'};
        char[] mnpr = {'m', 'n', 'p', 'r'};

        String value700t = field700.concatSubfields(t);

        if (value700t == null || value700t.equals("")) {
            return;
        }

        if (!field700.get2ndIndicator().equals("2")) {
            return;
        }

        // (4.3.3.1)
        for (String collectiveTitle :
                BatchLoading.COLLECTIVE_TITLES_BLACK_LIST) {

            if (value700t.trim().toLowerCase().equals(
                    collectiveTitle.toLowerCase())) {

                return;
            }
        }

        // (4.3.3.2)
        for (String form : BatchLoading.FORMS_LIST) {

            if (value700t.trim().toLowerCase().equals(form.toLowerCase())) {

                if (!field700.hasSubfields(mnpr)) {
                    // (4.3.3.2.1)
                    return;

                } else if (field700.hasSubfields(m)
                        && !field700.hasSubfields(npr)) {
                    // (4.3.3.2.2)
                    return;

                } else if (field700.hasSubfields(npr)) {

                    WorkField workField = new WorkField();
                    workField.setWorkDataField(field700);
                    if ("2".equals(group)) {
                        workField.setWorkIdentAlgorithm("4.3.3.2.3");
                    } else if ("3".equals(group)) {
                        workField.setWorkIdentAlgorithm("5.3.3.2.3");
                    } else if ("4".equals(group)) {
                        workField.setWorkIdentAlgorithm("6.3.3.2.3");
                    }
                    this.workFields.add(workField);
                    if (log.isInfoEnabled()) {
                        log.info("  identified single 700 group2 "
                                + workField.getWorkIdentAlgorithm() + ": "
                                + workField.getWorkDataField().toString());
                    }
                    return;

                }
            }
        }

        WorkField workField = new WorkField();
        workField.setWorkDataField(field700);
        if ("2".equals(group)) {
            workField.setWorkIdentAlgorithm("4.3.3.4");
        } else if ("3".equals(group)) {
            workField.setWorkIdentAlgorithm("5.3.3.4");
        } else if ("4".equals(group)) {
            workField.setWorkIdentAlgorithm("6.3.3.4");
        }
        this.workFields.add(workField);
        if (log.isInfoEnabled()) {
            log.info("  identified single 700 group2 "
                    + workField.getWorkIdentAlgorithm() + ": "
                    + workField.getWorkDataField().toString());
        }
        return;
    }
}
