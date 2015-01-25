// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.types;

// std
import java.sql.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

// local
import com.github.bjarneh.hour.util.htm;

/**
 * Represents a row on the day table.
 *
 * NOTE: to use this for more than one user,
 * (which is highly unlikely), a user should
 * be added to this table.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public class Day{


    public Date id;
    public List<Job> jobs;


    public void addJob(Job job){
        if( jobs == null ){
            jobs = new ArrayList<Job>();
        }
        jobs.add( job );
    }


    public static Day fromCalendar(Calendar cal){
        Day day = new Day(); 
        day.id  = new Date( cal.getTimeInMillis() );
        return day;
    }


    public htm.Node getHtml(){

        htm.Node table = htm.table()
                            .prop("class","timesheet");

        if( jobs != null && jobs.size() > 0 ){
            for(Job j: jobs){
                j.addInputRows(table);
                table.add(htm.tr().prop("class","padder"));
            }
        }
        // blank row should always be displayed
        Job.addBlankRows(table);
        return table;
    }
}
