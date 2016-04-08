// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.db;


// std
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;

// local
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.stal.types.Pay;

/**
 * The DB interface is basically used to allow multiple
 * database back-ends.
 *
 * We will only implement one version of this interface,
 * but the interface still makes sense, since it allows
 * us to switch database back-end, by providing another
 * DB implementation. You will see very similar methods
 * in the API class, the idea is that you can switch DB
 * by replacing the implementation of this interface.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public interface DB {

    /**
     * Save a new Day object.
     * @param day the to store.
     */
    public void createDay(Day day) throws Exception;

    /**
     * Get a Day object from primary key, date.
     * @param date primary key to fetch Day object from.
     * @return a Day object with list of jobs, or null.
     */
    public Day getDayFromPK(Date date) throws Exception;

    /**
     * Delete a Day object from the database.
     * @param day to delete from database.
     */
    public void deleteDay(Day day) throws Exception;

    /**
     * Store a modified version of a Day object.
     * @param day the day object to update.
     */
    public void updateDay(Day day) throws Exception;

    /**
     * Get a Pay object from primary key, company id.
     * @param company primary key to fetch Pay object from.
     * @return a Day object with list of jobs, or null.
     */
    public Pay getPayFromPK(String company) throws Exception;

    /**
     * Save a new Pay object in the database.
     * @param pay object to save. primary key to fetch Pay object from.
     */
    public void createPay(Pay pay) throws Exception;

    /**
     * Store a modified version of a Pay object.
     * @param pay the pay object to update.
     */
    public void updatePay(Pay pay) throws Exception;

    /**
     * Delete a Pay object from the database.
     * @param day to delete from the database.
     */
    public void deletePay(Pay pay) throws Exception;

    /**
     * Return a list of all company names.
     * @return a list of all company names
     */
    public ArrayList<String> getCompanyNames() throws Exception;

    /**
     * Get a Job object from primary key.
     * @param id primary key to fetch Job object from.
     * @return a Job object matching id, or null.
     */
    public Job getJobFromPK(long id) throws Exception;

    /**
     * Store a new Job object in the database.
     * @param job save in the database.
     */
    public void createJob(Job job) throws Exception;

    /**
     * Delete a Job object from the database.
     * @param job to delete from the database.
     */
    public void deleteJob(Job job) throws Exception;

    /**
     * Store a modified version of a Job object.
     * @param job the job object to update.
     */
    public void updateJob(Job job) throws Exception;

    /**
     * Return jobs based on interval.
     *
     * @param start first day of interval
     * @param stop last day of interval
     * @return all jobs in interval
     */
    public ArrayList<Job> intervalJobs(Day start, Day stop, String filter)
        throws Exception;

    /**
     * Return hours based on a company name, or all hours.
     *
     * @param company name of company to filter hours for
     *        if this is null we return hours for all companies
     * @return all hours used, or hours used for a single company
     */
    public double getJobTotalHours(String company) throws Exception;
}
