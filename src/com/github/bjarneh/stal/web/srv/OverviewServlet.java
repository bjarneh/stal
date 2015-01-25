// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.web.srv;

// std
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.sql.Timestamp;
import static java.lang.String.format;

// local
import com.github.bjarneh.stal.api.API;
import com.github.bjarneh.utilz.handy;
import com.github.bjarneh.hour.util.htm;
import com.github.bjarneh.utilz.Tuple;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Respond to /overview requests, filter on projects, count hours etc.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */


public class OverviewServlet extends CalendarServlet {

///     private static final Logger log =
///         Logger.getLogger( OverviewServlet.class.getName() );



    /**
     * Nothing yet.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        doGet(req, resp);
    }


    /**
     * Initialize objects forwarded to template (jsps/overview.jsp),
     * and forward request.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        Tuple<htm.Node, htm.Node> prevNext;

        Calendar calendar   = getCalendar(req);
        ArrayList<Job> jobs = jobsThisMonth(calendar);

        req.setAttribute("year", calendar.get(Calendar.YEAR));
        prevNext = getPrevNextLinks(calendar);
        req.setAttribute("prev", prevNext.getLeft());
        req.setAttribute("next", prevNext.getRight());

        req.setAttribute("overview", getOverviewTable(jobs));
        req.setAttribute("months", getMonths(calendar));
        req.setAttribute("contextPath",req.getContextPath());
        req.setAttribute("todayQ", queryToday(calendar));

        req.getRequestDispatcher("jsps/overview.jsp").forward(req, resp); 
    }


    @Override
    protected String pathQueryToday(Calendar cal){
        return htm.links.overview + queryToday(cal);
    }


    private ArrayList<Job> jobsThisMonth(Calendar cal)
        throws ServletException
    {

        Day first, last;
        ArrayList<Job> jobs = null;

        Date now = cal.getTime();
        int max  = cal.getActualMaximum(cal.DAY_OF_MONTH);

        cal.set(Calendar.DAY_OF_MONTH, 1);
        first = Day.fromCalendar( cal );
        cal.set(Calendar.DAY_OF_MONTH, max);
        last  = Day.fromCalendar( cal );

        System.out.printf(" %s -> %s \n", first.id, last.id);

        cal.setTime( now );


        try{
            jobs = api.intervalJobs(first, last);
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }

        return jobs;

    }


    public htm.Node getOverviewTable(List<Job> jobs){

        htm.Node table = htm.table().prop("class","overview");

        for(Job job: jobs){
            job.addOverviewRow( table );
        }

        return table;
    }
}
