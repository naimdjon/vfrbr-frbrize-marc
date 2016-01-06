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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Note: only the tab6 report has been converted to be of only scores.
 *
 * @author pbmcelwa
 */
public class ReportExtractionScore {

    private static final Logger log = Logger.getLogger(ReportExtractionScore.class);

    private final String CSV_PATH = "/usr/local/vfrbr/reports/score-manifs/csv/";

    private final String CSV_SEPR = "\t";

    private final String REP1_FILENAME = "report-1_extract.csv";

    private final String REP2_FILENAME = "report-2_extract.csv";

    private final String REP3_FILENAME = "report-3_extract.csv";

    private final String REP4_FILENAME = "report-4_extract.csv";

    private final String REP6_FILENAME = "report-6_extract.csv";

    private final double SAMPLING = 0.05;

    private Properties jdbcProps = new Properties();

    private Connection conn = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        log.info("== starting ==");

        ReportExtractionScore extract = new ReportExtractionScore();

        extract.extractReports();

        log.info("== done ==");
    }

    public void testStartup() {

        // load the jdbc properties
        jdbcProps = loadProps("jdbc.properties");
        log.info("-- jdbc properties loaded");

        this.conn = connect(jdbcProps);
        log.info("-- connected");

        log.info("   to db with "
                + testManifCount(conn)
                + " manifestations");

        // clean up
        this.disconnect(conn);
        log.info("-- disconnected");
    }

    public void extractReports() {

        // load the jdbc properties
        jdbcProps = loadProps("jdbc.properties");
        log.info("-- jdbc properties loaded");

        this.conn = connect(jdbcProps);
        log.info("-- connected");

        log.info("   to db with "
                + testManifCount(conn)
                + " manifestations");

//        extractReport1(conn);

//        extractReport2(conn);

//        extractReport3(conn);

//        extractReport4(conn);

        // Score Manifestations with no associated Work-Expressions
        extractReport6(conn);

        // clean up
        this.disconnect(conn);
        log.info("-- disconnected");

    }

    /**
     * Construct a new instance.
     */
    private ReportExtractionScore() {
    }

    /**
     * Load properties.
     * Should swallow exceptions, log them, and return empty Properties.
     *
     * @param propName String name of properties to load.
     * @return a Properties, of properties loaded, or empty if errors.
     */
    private Properties loadProps(String propName) {

        final Properties props = new Properties();

        try {
            final ClassLoader loader =
                    Thread.currentThread().getContextClassLoader();
            final InputStream inStream =
                    loader.getResourceAsStream(propName);
            if (inStream == null) {
                throw new Exception("Null stream for " + propName);
            } else {
                props.load(inStream);
            }
        } catch (Exception ex) {
            log.error("Error loading " + propName, ex);
        }

        return props;
    }

    /**
     * Load the jdbc properties
     */
    private void loadJdbcProps() {

        jdbcProps = loadProps("jdbc.properties");

        // echo the jdbc properties
        log.info("jdbcProps.toString: \n" + jdbcProps.toString());
    }

    /**
     * Open an sql connection to the data source
     */
    private Connection connect(Properties jdbcProps) {

        Connection connec = null;

        try {
            // load the DriverManager
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            // get the connection
            String url = jdbcProps.getProperty("javax.persistence.jdbc.url");
            String user = jdbcProps.getProperty("javax.persistence.jdbc.user");
            String password = System.getProperty("javax.persistence.jdbc.password", jdbcProps.getProperty("javax.persistence.jdbc.password"));

            log.info("-- connecting to: " + url);
            log.info("              as: " + user);

            StringBuilder param = new StringBuilder();
            param.append(url);
            param.append("&user=");
            param.append(user);
            param.append("&password=");
            param.append(password);

            connec = DriverManager.getConnection(param.toString());

        } catch (SQLException ex) {
            log.error("SQLException in connect(): " + ex.getMessage());
            log.error("SQLState: " + ex.getSQLState());
            log.error("VendorError: " + ex.getErrorCode());
        } catch (Exception ex) {
            log.error("Exception loading driver manager in connect(): ", ex);
        }


        return connec;
    }

    /**
     * clean up
     */
    private void disconnect(Connection conn) {

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                log.error("SQLException in disconnect(): " + ex.getMessage());
                log.error("SQLState: " + ex.getSQLState());
                log.error("VendorError: " + ex.getErrorCode());
            } finally {
                conn = null;
            }
        }

    }

    private int testManifCount(Connection conn) {

        Statement stmt = null;
        ResultSet rs = null;
        int count = -1;
        String qryStr = ""
                + "select count(*) \n"
                + "from MANIF_ENT";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(qryStr);
            rs.next();
            count = rs.getInt(1);
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            log.error("SQLException in testManifCount(): " + ex.getMessage());
            log.error("SQLState: " + ex.getSQLState());
            log.error("VendorError: " + ex.getErrorCode());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // ignore
                }
                rs = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
                stmt = null;
            }
        }

        return count;
    }

//    private void extractReport1(Connection conn) {
//
//        // test sample size
//
//        PreparedStatement predStmt = null;
//        ResultSet rs = null;
//        int totalRows = -1;
//        int sampleSize = -1;
//        int resultOffsets = -1;
//
//        String qryStrTotal = ""
//                + "select count(*) \n"
//                + "from FIZ_REP_G2BIB";
//
//        String qryStrRepData = ""
//                + "select \n"
//                + "  DB_ID, \n"
//                + "  CONTRIB_NAME, \n"
//                + "  CONTRIB_DATE, \n"
//                + "  CONTRIB_TYPE, \n"
//                + "  BIBREC_IDENT, \n"
//                + "  BIBFIELD_STRING \n"
//                + "from FIZ_REP_G2BIB \n";
//
//
//        try {
//            // get the total, sample size, and resultSet offset
//            predStmt = conn.prepareStatement(qryStrTotal);
//            rs = predStmt.executeQuery();
//            rs.next();
//            totalRows = rs.getInt(1);
//
//            sampleSize = (int) (totalRows * this.SAMPLING);
//            resultOffsets = (totalRows / sampleSize);
//
//            log.info(" ------------------");
//            log.info(" -- extractReport1:");
//            log.info("   -- sample size of " + sampleSize
//                    + " from " + this.SAMPLING
//                    + " of total " + totalRows);
//            log.info("   -- resultOffsets: " + resultOffsets);
//            log.info("   -- query string: \n"
//                    + qryStrRepData + "---");
//
//            // open the file for the csv data
//            PrintWriter out =
//                    new PrintWriter(new File(this.CSV_PATH, this.REP1_FILENAME),
//                                    "UTF-8");
//            log.info("    -- PrintWriter opened on "
//                    + this.CSV_PATH + " " + this.REP1_FILENAME);
//
//            // get the resultSet
//            predStmt = conn.prepareStatement(qryStrRepData);
//            rs = predStmt.executeQuery();
//
//            // step through the resultSet by offset
//            int lineCount = 0;
//            while (rs.relative(resultOffsets)) {
//                // -- SQL_ID
//                out.append(rs.getString(1));
//                // -- Contributor
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(2));
//                // -- Date
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(3));
//                // -- Type
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(4));
//                // -- "Type Set Correctly"
//                out.append(this.CSV_SEPR);
//                // -- Name Rec Source
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(5));
//                // -- Field Source
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(6));
//
//                out.println();
//                lineCount++;
//            }
//
//            out.flush();
//            out.close();
//            log.info("       flushed and closed");
//            log.info("       " + lineCount + " lines written");
//
//            rs.close();
//            predStmt.close();
//
//        } catch (SQLException ex) {
//            log.error("SQLException(s) in extractReport1()");
//            while (ex != null) {
//                log.error("  SQL State: " + ex.getSQLState());
//                log.error("       Code: " + ex.getErrorCode());
//                log.error("    Message: " + ex.getMessage());
//                log.error("  ---------");
//                ex = ex.getNextException();
//            }
//            log.error("----");
//
//        } catch (FileNotFoundException ex) {
//            log.error("Exception opening report file.", ex);
//
//        } catch (Exception ex) {
//            log.error("Exception.", ex);
//
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                rs = null;
//            }
//            if (predStmt != null) {
//                try {
//                    predStmt.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                predStmt = null;
//            }
//        }
//    }
//
//    private void extractReport2(Connection conn) {
//
//        PreparedStatement predStmt = null;
//        ResultSet rs = null;
//        int totalRows = -1;
//        int sampleSize = -1;
//        int resultOffsets = -1;
//
//        String qryStrTotal = ""
//                + "select count(*) \n"
//                + "from FIZ_REP_WORK";
//
//        String qryStrRepData = ""
//                + "select \n"
//                + "  DB_ID, \n"
//                + "  UNIFORM_TITLE, \n"
//                + "  CMP_AUTH_NAME, \n"
//                + "  IDENT_GROUP, \n"
//                + "  IDENT_ALGOR, \n"
//                + "  BIBREC_ID, \n"
//                + "  BIBFIELD_TAG, \n"
//                + "  MARC_FILENAME, \n"
//                + "  MARC_RECNUM \n"
//                + "from FIZ_REP_WORK \n";
//
//
//        try {
//            // get the total, sample size, and resultSet offset
//            predStmt = conn.prepareStatement(qryStrTotal);
//            rs = predStmt.executeQuery();
//            rs.next();
//            totalRows = rs.getInt(1);
//
//            sampleSize = (int) (totalRows * this.SAMPLING);
//            resultOffsets = (totalRows / sampleSize);
//
//            log.info(" ------------------");
//            log.info(" -- extractReport2:");
//            log.info("   -- sample size of " + sampleSize
//                    + " from " + this.SAMPLING
//                    + " of total " + totalRows);
//            log.info("   -- resultOffsets: " + resultOffsets);
//            log.info("   -- query string: \n"
//                    + qryStrRepData + "---");
//
//            // open the file for the csv data
//            PrintWriter out =
//                    new PrintWriter(new File(this.CSV_PATH, this.REP2_FILENAME),
//                                    "UTF-8");
//            log.info("    -- PrintWriter opened on "
//                    + this.CSV_PATH + " " + this.REP2_FILENAME);
//
//            // get the resultSet
//            predStmt = conn.prepareStatement(qryStrRepData);
//            rs = predStmt.executeQuery();
//
//            // step through the resultSet by offset
//            int lineCount = 0;
//            while (rs.relative(resultOffsets)) {
//                // -- SQL_ID
//                out.append(rs.getString(1));
//                // -- Uniform Title
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(2));
//                // -- Composer AuthName
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(3));
//                // -- Work Group
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(4));
//                // -- Algorithm Used
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(5));
//                // -- Work Record Id
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(6));
//                // -- Work Field Tag
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(7));
//                // -- MARC File:recNum
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(8));
//                out.append(":");
//                out.append(rs.getString(9));
//
//                out.println();
//                lineCount++;
//            }
//
//            out.flush();
//            out.close();
//            log.info("       flushed and closed");
//            log.info("       " + lineCount + " lines written");
//
//            rs.close();
//            predStmt.close();
//
//        } catch (SQLException ex) {
//            log.error("SQLException(s) in extractReport2()");
//            while (ex != null) {
//                log.error("  SQL State: " + ex.getSQLState());
//                log.error("       Code: " + ex.getErrorCode());
//                log.error("    Message: " + ex.getMessage());
//                log.error("  ---------");
//                ex = ex.getNextException();
//            }
//            log.error("----");
//
//        } catch (FileNotFoundException ex) {
//            log.error("Exception opening report file.", ex);
//
//        } catch (Exception ex) {
//            log.error("Exception.", ex);
//
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                rs = null;
//            }
//            if (predStmt != null) {
//                try {
//                    predStmt.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                predStmt = null;
//            }
//        }
//    }
//
//    private void extractReport3(Connection conn) {
//
//        PreparedStatement predStmt = null;
//        ResultSet rs = null;
//        int totalRows = -1;
//        int sampleSize = -1;
//        int resultOffsets = -1;
//
//        String qryStrTotal = ""
//                + "select count(*) \n"
//                + "from FIZ_REP_WORK "
//                + "where AUTHREC_ID is null";
//
//        String qryStrRepData = ""
//                + "select \n"
//                + "  DB_ID, \n"
//                + "  UNIFORM_TITLE, \n"
//                + "  DATE_TEXT, \n"
//                + "  CMP_AUTH_NAME, \n"
//                + "  BIBREC_ID, \n"
//                + "  BIBFIELD_TAG, \n"
//                + "  MARC_FILENAME, \n"
//                + "  MARC_RECNUM \n"
//                + "from FIZ_REP_WORK \n"
//                + "where AUTHREC_ID is null \n";
//
//
//        try {
//            // get the total, sample size, and resultSet offset
//            predStmt = conn.prepareStatement(qryStrTotal);
//            rs = predStmt.executeQuery();
//            rs.next();
//            totalRows = rs.getInt(1);
//
//            sampleSize = (int) (totalRows * this.SAMPLING);
//            resultOffsets = (totalRows / sampleSize);
//
//            log.info(" ------------------");
//            log.info(" -- extractReport3:");
//            log.info("   -- sample size of " + sampleSize
//                    + " from " + this.SAMPLING
//                    + " of total " + totalRows);
//            log.info("   -- resultOffsets: " + resultOffsets);
//            log.info("   -- query string: \n"
//                    + qryStrRepData + "---");
//
//            // open the file for the csv data
//            PrintWriter out =
//                    new PrintWriter(new File(this.CSV_PATH, this.REP3_FILENAME),
//                                    "UTF-8");
//            log.info("    -- PrintWriter opened on "
//                    + this.CSV_PATH + " " + this.REP3_FILENAME);
//
//            // get the resultSet
//            predStmt = conn.prepareStatement(qryStrRepData);
//            rs = predStmt.executeQuery();
//
//            // step through the resultSet by offset
//            int lineCount = 0;
//            while (rs.relative(resultOffsets)) {
//                // -- SQL_ID
//                out.append(rs.getString(1));
//                // -- Uniform Title
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(2));
//                // -- Work Date
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(3));
//                // -- Composer AuthName
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(4));
//                // -- Title Record Source
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(5));
//                // -- Title Field Tag
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(6));
//                // -- MARC File:recNum
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(7));
//                out.append(":");
//                out.append(rs.getString(8));
//
//                out.println();
//                lineCount++;
//            }
//
//            out.flush();
//            out.close();
//            log.info("       flushed and closed");
//            log.info("       " + lineCount + " lines written");
//
//            rs.close();
//            predStmt.close();
//
//        } catch (SQLException ex) {
//            log.error("SQLException(s) in extractReport3()");
//            while (ex != null) {
//                log.error("  SQL State: " + ex.getSQLState());
//                log.error("       Code: " + ex.getErrorCode());
//                log.error("    Message: " + ex.getMessage());
//                log.error("  ---------");
//                ex = ex.getNextException();
//            }
//            log.error("----");
//
//        } catch (FileNotFoundException ex) {
//            log.error("Exception opening report file.", ex);
//
//        } catch (Exception ex) {
//            log.error("Exception.", ex);
//
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                rs = null;
//            }
//            if (predStmt != null) {
//                try {
//                    predStmt.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                predStmt = null;
//            }
//        }
//    }
//
//    private void extractReport4(Connection conn) {
//
//        PreparedStatement predStmt = null;
//        ResultSet rs = null;
//        int totalRows = -1;
//        int sampleSize = -1;
//        int resultOffsets = -1;
//
//        String qryStrTotal = ""
//                + "select count(*) \n"
//                + "from FIZ_REP_WORK \n"
//                + "where CMP_AUTH_NAME is null \n";
//
//        String qryStrRepData = ""
//                + "select \n"
//                + "  DB_ID, \n"
//                + "  UNIFORM_TITLE, \n"
//                + "  BIBREC_ID, \n"
//                + "  BIBFIELD_TAG, \n"
//                + "  MARC_FILENAME, \n"
//                + "  MARC_RECNUM \n"
//                + "from FIZ_REP_WORK \n"
//                + "where CMP_AUTH_NAME is null \n";
//
//
//        try {
//            // get the total, sample size, and resultSet offset
//            predStmt = conn.prepareStatement(qryStrTotal);
//            rs = predStmt.executeQuery();
//            rs.next();
//            totalRows = rs.getInt(1);
//
//            if (totalRows >= 200) {
//                sampleSize = (int) (totalRows * this.SAMPLING);
//                resultOffsets = (totalRows / sampleSize);
//            } else {
//                // for less than 200, do all
//                sampleSize = totalRows;
//                resultOffsets = 1;
//            }
//
//            log.info(" ------------------");
//            log.info(" -- extractReport4:");
//            log.info("   -- sample size of " + sampleSize
//                    + " from " + this.SAMPLING
//                    + " of total " + totalRows);
//            log.info("   -- resultOffsets: " + resultOffsets);
//            log.info("   -- query string: \n"
//                    + qryStrRepData + "---");
//
//            // open the file for the csv data
//            PrintWriter out =
//                    new PrintWriter(new File(this.CSV_PATH, this.REP4_FILENAME),
//                                    "UTF-8");
//            log.info("    -- PrintWriter opened on "
//                    + this.CSV_PATH + " " + this.REP4_FILENAME);
//
//            // get the resultSet
//            predStmt = conn.prepareStatement(qryStrRepData);
//            rs = predStmt.executeQuery();
//
//            // step through the resultSet by offset
//            int lineCount = 0;
//            while (rs.relative(resultOffsets)) {
//                // -- SQL_ID
//                out.append(rs.getString(1));
//                // -- Uniform Title
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(2));
//                // -- Title Record Source
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(3));
//                // -- Title Field Tag
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(4));
//                // -- MARC File:recNum
//                out.append(this.CSV_SEPR);
//                out.append(rs.getString(5));
//                out.append(":");
//                out.append(rs.getString(6));
//
//                out.println();
//                lineCount++;
//            }
//
//            out.flush();
//            out.close();
//            log.info("       flushed and closed");
//            log.info("       " + lineCount + " lines written");
//
//            rs.close();
//            predStmt.close();
//
//        } catch (SQLException ex) {
//            log.error("SQLException(s) in extractReport4()");
//            while (ex != null) {
//                log.error("  SQL State: " + ex.getSQLState());
//                log.error("       Code: " + ex.getErrorCode());
//                log.error("    Message: " + ex.getMessage());
//                log.error("  ---------");
//                ex = ex.getNextException();
//            }
//            log.error("----");
//
//        } catch (FileNotFoundException ex) {
//            log.error("Exception opening report file.", ex);
//
//        } catch (Exception ex) {
//            log.error("Exception.", ex);
//
//        } finally {
//            if (rs != null) {
//                try {
//                    rs.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                rs = null;
//            }
//            if (predStmt != null) {
//                try {
//                    predStmt.close();
//                } catch (SQLException ex) {
//                    // ignore
//                }
//                predStmt = null;
//            }
//        }
//    }

    private void extractReport6(Connection conn) {

        PreparedStatement predStmt = null;
        ResultSet rs = null;
        int totalRows = -1;
        int sampleSize = -1;
        int resultOffsets = -1;

        String qryStrTotal = ""
                + "select count(*) \n"
                + "from FIZ_REP_MANIFNOWORK, \n"
                + "     MANIF_ENT \n"
                + "where MANIF_ENT_ID = DB_ID \n"
                + "  and EXPR_FORM in (\"notated music\", \"manuscript notated music\")";

        String qryStrRepData = ""
                + "select \n"
                + "  BIBREC_GROUP, \n"
                + "  BIBREC_ID, \n"
                + "  DB_ID, \n"
                + "  TITLE, \n"
                + "  CONTRIB_AUTHNAME, \n"
                + "  CONTRIB_TYPE, \n"
                + "  CONTRIB_ROLE, \n"
                + "  MARC_FILENAME, \n"
                + "  MARC_RECNUM \n"
                + "from FIZ_REP_MANIFNOWORK, \n"
                + "     MANIF_ENT \n"
                + "where MANIF_ENT_ID = DB_ID \n"
                + "  and EXPR_FORM in (\"notated music\", \"manuscript notated music\")";


        try {
            // get the total, sample size, and resultSet offset
            predStmt = conn.prepareStatement(qryStrTotal);
            rs = predStmt.executeQuery();
            rs.next();
            totalRows = rs.getInt(1);

            sampleSize = (int) (totalRows * this.SAMPLING);
            resultOffsets = (totalRows / sampleSize);

            log.info(" ------------------");
            log.info(" -- extractReport6:");
            log.info("   -- sample size of " + sampleSize
                    + " from " + this.SAMPLING
                    + " of total " + totalRows);
            log.info("   -- resultOffsets: " + resultOffsets);
            log.info("   -- query string: \n"
                    + qryStrRepData + "---");

            // open the file for the csv data
            PrintWriter out =
                    new PrintWriter(new File(this.CSV_PATH, this.REP6_FILENAME),
                            "UTF-8");
            log.info("    -- PrintWriter opened on "
                    + this.CSV_PATH + " " + this.REP6_FILENAME);

            // get the resultSet
            predStmt = conn.prepareStatement(qryStrRepData);
            rs = predStmt.executeQuery();

            // step through the resultSet by offset
            int lineCount = 0;
            while (rs.relative(resultOffsets)) {
                // -- Record Group
                out.append(rs.getString(1));
                // -- Record Id
                out.append(this.CSV_SEPR);
                out.append(rs.getString(2));
                // -- SQL_ID
                out.append(this.CSV_SEPR);
                out.append(rs.getString(3));
                // -- Manifestation Title
                out.append(this.CSV_SEPR);
                out.append(rs.getString(4));
                // -- Contributor AuthName
                out.append(this.CSV_SEPR);
                out.append(rs.getString(5));
                // -- Contrib. Type
                out.append(this.CSV_SEPR);
                out.append(rs.getString(6));
                // -- Contrib. Role
                out.append(this.CSV_SEPR);
                out.append(rs.getString(7));
                // -- MARC File:recNum
                out.append(this.CSV_SEPR);
                out.append(rs.getString(8));
                out.append(":");
                out.append(rs.getString(9));

                out.println();
                lineCount++;
            }

            out.flush();
            out.close();
            log.info("       flushed and closed");
            log.info("       " + lineCount + " lines written");

            rs.close();
            predStmt.close();

        } catch (SQLException ex) {
            log.error("SQLException(s) in extractReport6()");
            while (ex != null) {
                log.error("  SQL State: " + ex.getSQLState());
                log.error("       Code: " + ex.getErrorCode());
                log.error("    Message: " + ex.getMessage());
                log.error("  ---------");
                ex = ex.getNextException();
            }
            log.error("----");

        } catch (FileNotFoundException ex) {
            log.error("Exception opening report file.", ex);

        } catch (Exception ex) {
            log.error("Exception.", ex);

        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // ignore
                }
                rs = null;
            }
            if (predStmt != null) {
                try {
                    predStmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
                predStmt = null;
            }
        }
    }
}
