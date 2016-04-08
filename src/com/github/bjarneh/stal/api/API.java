// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.api;

// stdlib
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.sql.Date;

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// local
import com.github.bjarneh.stal.db.DB;
import com.github.bjarneh.stal.db.hsql.HsqlDB;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.stal.types.Pay;


/**
 * Singleton that wraps a DB implementation to allow injection.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class API {

    static final Logger log = Log.getLogger(API.class);

    private static DB db = null;
    private static API api = null;


    private API(){}


    public static API getAPI(DB impl) throws Exception {
        if( api == null ){
            api = new API();
            if( impl == null ){
                api.db = HsqlDB.getDB();
            }
            log.info("API loaded.... ");
        }
        return api;
    }


    public static API getAPI() throws Exception {
        return getAPI(null);
    }



    /**
     * Wrapper for {@link #DB.createDay}
     */
    public void createDay(Day day) throws Exception {
        db.createDay(day);
    }

    // just a wrapper to get a Day object from Calendar.getTime
    public Day getDayFromPK(Calendar cal) throws Exception {
        return db.getDayFromPK(new Date(cal.getTime().getTime()));
    }

    /**
     * Wrapper for {@link #DB.getDayFromPK}
     */
    public Day getDayFromPK(Date date) throws Exception {
        return db.getDayFromPK(date);
    }

    /**
     * Wrapper for {@link #DB.deleteDay}
     */
    public void deleteDay(Day day) throws Exception {
        db.deleteDay(day);
    }

    /**
     * Wrapper for {@link #DB.updateDay}
     */
    public void updateDay(Day day) throws Exception {
        db.updateDay(day);
    }

    /**
     * Wrapper for {@link #DB.getPayFromPK}
     */
    public Pay getPayFromPK(String company) throws Exception {
        return db.getPayFromPK(company);
    }

    /**
     * Wrapper for {@link #DB.createPay}
     */
    public void createPay(Pay pay) throws Exception {
        db.createPay(pay);
    }

    /**
     * Wrapper for {@link #DB.updatePay}
     */
    public void updatePay(Pay pay) throws Exception {
        db.updatePay(pay);
    }

    /**
     * Wrapper for {@link #DB.deletePay}
     */
    public void deletePay(Pay pay) throws Exception {
        db.deletePay(pay);
    }

    /**
     * Wrapper for {@link #DB.getCompanyNames}
     */
    public ArrayList<String> getCompanyNames() throws Exception {
        return db.getCompanyNames();
    }

    /**
     * Wrapper for {@link #DB.getJobFromPK}
     */
    public Job getJobFromPK(long id) throws Exception {
        return db.getJobFromPK(id);
    }

    /**
     * Wrapper for {@link #DB.createJob}
     */
    public void createJob(Job job) throws Exception {
        db.createJob(job);
    }

    /**
     * Wrapper for {@link #DB.deleteJob}
     */
    public void deleteJob(Job job) throws Exception {
        db.deleteJob(job);
    }

    /**
     * Wrapper for {@link #DB.updateJob}
     */
    public void updateJob(Job job) throws Exception {
        db.updateJob(job);
    }

    /**
     * Wrapper for {@link #DB.intervalJobs}
     */
    public ArrayList<Job> intervalJobs(Day start, Day stop, String filter)
        throws Exception
    {
        return db.intervalJobs(start, stop, filter);
    }

    /**
     * Wrapper for {@link #DB.getJobTotalHours}
     */
    public double getJobTotalHours(String company) throws Exception {
        return db.getJobTotalHours(company);
    }
}
