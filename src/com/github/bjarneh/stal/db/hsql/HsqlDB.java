// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.db.hsql;

// std
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;
import java.sql.Types;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import static java.lang.String.format;
import java.io.IOException;

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// libb
import com.github.bjarneh.utilz.io;
import com.github.bjarneh.utilz.res;
import com.github.bjarneh.utilz.globals;
import com.github.bjarneh.utilz.handy;

// local
import com.github.bjarneh.stal.db.DB;
import com.github.bjarneh.stal.types.User;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.stal.types.Pay;

/**
 * An implementation of the DB interface using hsqldb.
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class HsqlDB implements DB {
    
    static final Logger log = Log.getLogger( HsqlDB.class );

    private static String DB_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    private static String DB_URL    = null;
    private static String DB_USER   = "SA";
    private static String DB_PASS   = "";


    // singleton object
    private static HsqlDB db = null;


    private HsqlDB(){
        ;
    }


    private void init()
        throws ClassNotFoundException, SQLException, IOException
    {
        // make sure driver is registered
        Class.forName(DB_DRIVER);
        // this can be set by command line arg or config
        DB_URL = globals.getStr("DB_URL");
        // log some action
        log.info(String.format("HsqlDB.init(%s, %s)", DB_DRIVER, DB_URL ));
        // assert that the database is as expected
        assertOk();
    }


    public static DB getDB() 
        throws ClassNotFoundException, SQLException, IOException
    {
        if( db == null ){
            db = new HsqlDB();
            db.init();
        }
        return db;
    }


    private Connection getConn() throws SQLException {
         return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }


    // create tables if not already here

    private void assertOk()
        throws SQLException, IOException
    {
        try( Connection conn = getConn() ) {

            PreparedStatement pstmt =
                conn.prepareStatement(SQL.OUR_TABLE_NAMES);
            ResultSet result = pstmt.executeQuery();

            int tableCount = 0;

            if( result.next() ){
                tableCount = result.getInt("MYTOTAL");
            }
            
            if( tableCount != 4 ){
                String batchQ = 
                    new String(io.wget(
                            res.get().url("misc/sql/hour.sql")));
                String[] q = batchQ.split("---------------------------------");
                for(int i = 0; i < q.length; i++){
                    conn.prepareStatement( q[i] ).executeUpdate();
                }
                log.info("Created missing tables");
            } else {
                log.info("Table structure in tact");
            }
        }
    }
 

    // start User

    /**
     * {@inheritDoc}
     */
    public User getUserFromPK(String pk)
        throws SQLException
    {
        try(Connection conn = getConn()){
            conn.setReadOnly( true );
            return getUserFromPK( pk, conn );
        }
    }


    private User getUserFromPK(String pk, Connection conn)
        throws SQLException
    {
        User user = null;

        try(PreparedStatement pstmt =
                conn.prepareStatement(SQL.USER_FROM_PK) )
        {
            pstmt.setString(1, pk);
            ResultSet result = pstmt.executeQuery();

            if( result.next() ){
                user = fillUserFromResultset(result);
            }

        }

        return user;
    }


    private User fillUserFromResultset(ResultSet result)
        throws SQLException
    {
        User user = new User();
        user.id   = result.getString("ID");
        user.name = result.getString("NAME");
        return user;
    }



    // end User


    // Day start

    /**
     * {@inheritDoc}
     */
    public void createDay(Day day)
        throws SQLException
    {
        try(Connection conn = getConn()){
            createDay(day, conn);
        }
    }


    private void createDay(Day day, Connection conn)
        throws SQLException
    {
        Day dbDay = getDayFromPK( day.id, conn );

        if( dbDay == null ){

            PreparedStatement pstmt = 
                conn.prepareStatement(SQL.DAY_CREATE);

            pstmt.setDate(1, day.id);
            pstmt.executeUpdate();
            pstmt.close();

        }

        if( day.jobs != null && day.jobs.size() > 0 ){
            for(Job job: day.jobs){
                createJob(job, conn);
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public Day getDayFromPK(Date date)
        throws SQLException
    {
        Day day = null;
        try(Connection conn = getConn()){
            day = getDayFromPK(date, conn);
        }
        return day;
    }


    private Day getDayFromPK(Date date, Connection conn)
        throws SQLException
    {
        Day day = null;

        PreparedStatement pstmt = conn.prepareStatement(SQL.DAY_FROM_PK);
        pstmt.setDate(1, date);

        ResultSet result = pstmt.executeQuery();

        if( result.next() ){
            day = new Day();
            day.id = result.getDate("id");
        }

        pstmt.close();

        if( day != null ){
            day.jobs = dayJobs(day, conn);
        }

        return day;
    }


    /**
     * {@inheritDoc}
     */
    public void deleteDay(Day day)
        throws SQLException
    {
        try(Connection conn = getConn()){
            deleteDay(day, conn);
        }
    }


    private void deleteDay(Day day, Connection conn)
        throws SQLException
    {
        PreparedStatement pstmt = conn.prepareStatement(SQL.DAY_DELETE);
        pstmt.setDate(1, day.id);
        pstmt.executeUpdate();
        pstmt.close();
    }


    /**
     * {@inheritDoc}
     */
    public void updateDay(Day day)
        throws SQLException
    {
        try(Connection conn = getConn()){
            updateDay(day, conn);
        }
    }


    private void updateDay(Day day, Connection conn)
        throws SQLException
    {
        List<Job> jobs = day.jobs;
        day.jobs = null;

        // ignores duplicates, when jobs == null
        createDay(day, conn);

        // save jobs if we have any
        if( jobs != null && jobs.size() > 0 ){
            for(Job j: jobs){
                if(j.id != null){
                    if( j.id > 0 ){
                        updateJob(j, conn);
                    }else{
                        j.id = j.id * -1;
                        deleteJob(j, conn);
                    }
                }else{
                    createJob(j, conn);
                }
            }
        }
    }


    // Day end


    // Pay start

    /**
     * {@inheritDoc}
     */
    public Pay getPayFromPK(String company)
        throws SQLException
    {
        Pay pay = null;
        try(Connection conn = getConn()){
            pay = getPayFromPK(company, conn);
        }
        return pay;
    }


    private Pay getPayFromPK(String company, Connection conn)
        throws SQLException
    {
        Pay pay = null;

        PreparedStatement pstmt = conn.prepareStatement(SQL.PAY_FROM_PK);
        pstmt.setString(1, company);
        ResultSet result = pstmt.executeQuery();

        if( result.next() ){
            pay             = new Pay();
            pay.company     = result.getString("company");
            pay.contact     = result.getString("contact");
            pay.address     = result.getString("address");
            pay.email       = result.getString("email");
            pay.phone       = result.getString("phone");
            pay.assignment  = result.getString("assignment");
        }

        pstmt.close();

        return pay;
    }

    
    /**
     * {@inheritDoc}
     */
    public void createPay(Pay pay)
        throws SQLException
    {
        try(Connection conn = getConn()){
            createPay(pay, conn);
        }
    }


    private void createPay(Pay pay, Connection conn)
        throws SQLException
    {
        int i = 1;
        PreparedStatement pstmt = conn.prepareStatement(SQL.PAY_CREATE);
        pstmt.setString(i++, pay.company);
        pstmt.setString(i++, pay.contact);
        pstmt.setString(i++, pay.address);
        pstmt.setString(i++, pay.email);
        pstmt.setString(i++, pay.phone);
        pstmt.setString(i++, pay.assignment);
        pstmt.executeUpdate();
        pstmt.close();
    }


    /**
     * {@inheritDoc}
     */
    public void updatePay(Pay pay)
        throws SQLException
    {
        try(Connection conn = getConn()){
            updatePay(pay, conn);
        }
    }


    private void updatePay(Pay pay, Connection conn)
        throws SQLException
    {

        PreparedStatement pstmt =
            conn.prepareStatement(SQL.PAY_UPDATE);

        int i = 1;
        pstmt.setString(i++, pay.contact);
        pstmt.setString(i++, pay.address);
        pstmt.setString(i++, pay.email);
        pstmt.setString(i++, pay.phone);
        pstmt.setString(i++, pay.assignment);

        pstmt.setString(i++, pay.company);

        pstmt.executeUpdate();
        pstmt.close();

    }



    /**
     * {@inheritDoc}
     */
    public void deletePay(Pay pay)
        throws SQLException
    {
        try(Connection conn = getConn()){
            deletePay(pay, conn);
        }
    }


    private void deletePay(Pay pay, Connection conn)
        throws SQLException
    {
        PreparedStatement pstmt = conn.prepareStatement(SQL.PAY_DELETE);
        pstmt.setString(1, pay.company);
        pstmt.executeUpdate();
        pstmt.close();
    }


    private void assertCompanyExists(String company, Connection conn)
        throws SQLException
    {
        if( company == null ){ return; }

        Pay pay = getPayFromPK(company, conn);
        if( pay == null ){
            pay = new Pay();
            pay.company = company;
            createPay(pay, conn);
        }
    }


    /**
     * {@inheritDoc}
     */
    public ArrayList<String> getCompanyNames()
        throws SQLException
    {
        try(Connection conn = getConn()){
            return getCompanyNames(conn);
        }
    }


    private ArrayList<String> getCompanyNames(Connection conn)
        throws SQLException
    {
        ArrayList<String> names = new ArrayList<String>();

        PreparedStatement pstmt = conn.prepareStatement(SQL.PAY_PK_LIST);
        ResultSet result = pstmt.executeQuery();

        while(result.next()){
            names.add( result.getString("company") );
        }

        return names;
    }


    // Pay end



    // Job start

    /**
     * {@inheritDoc}
     */
    public Job getJobFromPK(long id)
        throws SQLException
    {
        Job job = null;
        try(Connection conn = getConn()){
            job = getJobFromPK(id, conn);
        }
        return job;
    }

    
    private Job getJobFromPK(long id, Connection conn)
        throws SQLException
    {
        Job job = null;

        PreparedStatement pstmt = conn.prepareStatement(SQL.JOB_FROM_PK);
        pstmt.setLong(1, id);
        ResultSet result = pstmt.executeQuery();

        if( result.next() ){
            job = fillJobFromResult(result);
        }

        pstmt.close();

        return job;
    }


    private Job fillJobFromResult(ResultSet result)
        throws SQLException
    {
        Job job     = new Job();
        job.id      = result.getLong("id");
        job.dayId   = result.getDate("dayid");
        job.company = result.getString("company");
        job.start   = result.getTimestamp("start");
        job.stop    = result.getTimestamp("stop");
        job.total   = result.getDouble("total");
        if( result.wasNull() ){ job.total = null; }
        job.what    = result.getString("what");
        return job;
    }


    /**
     * {@inheritDoc}
     */
    public void createJob(Job job)
        throws SQLException
    {
        try(Connection conn = getConn()){
            createJob(job, conn);
        }
    }


    private void createJob(Job job, Connection conn)
        throws SQLException
    {
        int i = 1;

        job.setTimeUsed();
        assertCompanyExists(job.company, conn);

        PreparedStatement pstmt = conn.prepareStatement(SQL.JOB_CREATE);
        pstmt.setDate(i++, job.dayId);
        pstmt.setString(i++, job.company);
        pstmt.setTimestamp(i++, job.start);
        pstmt.setTimestamp(i++, job.stop);
        if( job.total != null ){
            pstmt.setDouble(i++, job.total);
        }else{
            pstmt.setNull(i++, Types.DOUBLE);
        }
        pstmt.setString(i++, job.what);
        
        pstmt.executeUpdate();
        pstmt.close();
    }



    /**
     * {@inheritDoc}
     */
    public void deleteJob(Job job)
        throws SQLException
    {
        try(Connection conn = getConn()){
            deleteJob(job, conn);
        }
    }


    private void deleteJob(Job job, Connection conn)
        throws SQLException
    {
        PreparedStatement pstmt = conn.prepareStatement(SQL.JOB_DELETE);
        pstmt.setLong(1, job.id);
        pstmt.executeUpdate();
        pstmt.close();
    }

   
    /**
     * {@inheritDoc}
     */
    public ArrayList<Job> dayJobs(Day day)
        throws SQLException
    {
       ArrayList<Job> jobs = null;
       try(Connection conn = getConn()){
           conn.setReadOnly(true);
           jobs = dayJobs(day, conn);
       }
       return jobs;
    }


    private ArrayList<Job> dayJobs(Day day, Connection conn)
        throws SQLException
    {
        if( day == null || day.id == null ){ return null; }

        ArrayList<Job> jobs = new ArrayList<Job>();

        PreparedStatement pstmt =
            conn.prepareStatement(SQL.JOB_FROM_DATE);

        pstmt.setDate(1, day.id);

        ResultSet result = pstmt.executeQuery();
        
        while(result.next()){
            jobs.add( fillJobFromResult(result) );
        }

        return jobs;
    }

    
    /**
     * {@inheritDoc}
     */
    public void updateJob(Job job)
        throws SQLException
    {
        try(Connection conn = getConn()){
            updateJob(job, conn);
        }
    }


    private void updateJob(Job job, Connection conn)
        throws SQLException
    {

        job.setTimeUsed();
        assertCompanyExists(job.company, conn);

        PreparedStatement pstmt =
            conn.prepareStatement(SQL.JOB_UPDATE);

        int i = 1;
        if( job.dayId != null ){
            pstmt.setDate(i++, job.dayId);
        }else{
            pstmt.setNull(i++, Types.DATE);
        }
        pstmt.setString(i++, job.company);
        pstmt.setTimestamp(i++, job.start);
        pstmt.setTimestamp(i++, job.stop);
        if( job.total != null ){
            pstmt.setDouble(i++, job.total);
        }else{
            pstmt.setNull(i++, Types.DOUBLE);
        }
        pstmt.setString(i++, job.what);
        pstmt.setLong(i++, job.id);
        pstmt.executeUpdate();
    }


    /**
     * {@inheritDoc}
     */
    public ArrayList<Job> intervalJobs(Day start, Day stop)
        throws SQLException
    {
        ArrayList<Job> jobs = null;

        try(Connection conn = getConn()){
            jobs = intervalJobs(start, stop, conn);
        }

        return jobs;
    }


    private ArrayList<Job> intervalJobs(Day start, Day stop, Connection conn)
        throws SQLException
    {
        ArrayList<Job> jobs = new ArrayList<Job>();

        PreparedStatement pstmt = conn.prepareStatement(SQL.JOB_INTERVAL);
        pstmt.setDate(1, start.id);
        pstmt.setDate(2, stop.id);

        ResultSet result = pstmt.executeQuery();
        while( result.next() ){
            jobs.add( fillJobFromResult( result ) );
        }

        return jobs;

    }


    // Job end



    final static class SQL {

        // start misc

        final static String OUR_TABLE_NAMES =
            "  SELECT                                      "+
            "         COUNT(*) AS MYTOTAL                  "+
            "  FROM                                        "+
            "         INFORMATION_SCHEMA.SYSTEM_TABLES     "+
            "  WHERE                                       "+
            "         TABLE_SCHEM = 'PUBLIC'               "+
            "  AND                                         "+
            "         TABLE_NAME IN ('JOB', 'PAY', 'DAY', 'USER')  ";

        //TODO REMOVE USER


        // end misc



        // start User

        final static String USER_FROM_PK = "SELECT * FROM USER WHERE ID = ? ";

        // end User
    

        // Day start  TODO use MERGE statement to simulate ON DUPLICATE..

        static final String DAY_CREATE =
            " INSERT INTO DAY (ID) VALUES (?) ";

        static final String DAY_FROM_PK =
            " SELECT * FROM DAY WHERE ID=? ";

        static final String DAY_DELETE =
            " DELETE FROM DAY WHERE ID=? ";

        // Day end


        // Pay start

        static final String PAY_FROM_PK =
            " SELECT * FROM PAY WHERE COMPANY=? ";

        static final String PAY_CREATE =
            " INSERT INTO PAY                                  "+
            " (COMPANY,CONTACT,ADDRESS,EMAIL,PHONE,ASSIGNMENT) "+
            " VALUES (?,?,?,?,?,?)                             ";
        
        static final String PAY_DELETE =
            " DELETE FROM PAY WHERE COMPANY=? ";

        static final String PAY_PK_LIST =
            " SELECT PAY.COMPANY FROM PAY ";

        static final String PAY_UPDATE =
            " UPDATE PAY SET     "+
            "  CONTACT        =?,"+
            "  ADDRESS        =?,"+
            "  EMAIL          =?,"+
            "  PHONE          =?,"+
            "  ASSIGNMENT     =? "+
            " WHERE    COMPANY=? ";


        // Pay end


        // Job start
        
        static final String JOB_FROM_PK =
            " SELECT * FROM JOB WHERE ID=? ";


        static final String JOB_CREATE =
            " INSERT INTO JOB                       "+
            " (DAYID,COMPANY,START,STOP,TOTAL,WHAT) "+
            " VALUES (?,?,?,?,?,?)                  ";


        static final String JOB_UPDATE =
            " UPDATE JOB SET     "+
            "  DAYID          =?,"+
            "  COMPANY        =?,"+
            "  START          =?,"+
            "  STOP           =?,"+
            "  TOTAL          =?,"+
            "  WHAT           =? "+
            " WHERE         ID=? ";


        static final String JOB_DELETE =
            " DELETE FROM JOB WHERE ID=? ";

        static final String JOB_FROM_DATE =
            " SELECT * FROM JOB WHERE DAYID=? ";

        static final String JOB_INTERVAL =
            " SELECT * FROM JOB WHERE DAYID >=? AND DAYID <=? ";

        // Job end
        
    }
}
