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

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// local
import com.github.bjarneh.stal.api.API;
import com.github.bjarneh.utilz.handy;
import com.github.bjarneh.hour.util.htm;
import com.github.bjarneh.utilz.Tuple;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;

/**
 * Respond to /overview requests, filter on projects, count hours etc.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class OverviewServlet extends CalendarServlet {


    static final Logger log = Log.getLogger( OverviewServlet.class );


    /**
     * Nothing yet.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        resp.setStatus(resp.SC_OK);
        resp.setContentType("text/csv"); 
        resp.setHeader("Content-Disposition",
                "inline; filename=overview_"+ System.currentTimeMillis() + ".csv");

        String filter       = req.getParameter("filter");
        Calendar calendar   = getCalendar(req);
        ArrayList<Job> jobs = jobsThisMonth(calendar, filter);

        ServletOutputStream out = resp.getOutputStream();
        out.print("Customer;Date;From;To;Hours;Description\r\n");
        double tot = 0.0;
        for(Job job: jobs){
            out.print(String.format("\"%s\";%s;%tR;%tR;%s;\"%s\"\r\n",
                    job.company,job.dayId,job.start,job.stop,job.total,
                    job.what.replace("\"","'")
                            .replace("\n"," ")
                            .replace("\t"," ")));
            tot += job.total;
        }
        if(tot > 0.0){
            out.print(";;;;"+tot+";\r\n");
        }
        out.flush();
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

        String filter       = req.getParameter("filter");
        Calendar calendar   = getCalendar(req);
        ArrayList<Job> jobs = jobsThisMonth(calendar, filter);
        String todayQ       = queryToday(calendar);

        req.setAttribute("todayQ", todayQ);
         // TODO escape characters that can cause problems in HTML
        req.setAttribute("filter", filter);

        if(filter == null){
            req.setAttribute("divstyle", "display:block");
        }else{
            req.setAttribute("divstyle", "display:none");
            req.setAttribute("overviewstyle", "max-width:97% !important");
        }

        req.setAttribute("year", calendar.get(Calendar.YEAR));
        prevNext = getPrevNextLinks(calendar);
        req.setAttribute("prev", prevNext.getLeft());
        req.setAttribute("next", prevNext.getRight());

        req.setAttribute("overview", getOverviewTable(jobs, todayQ, filter));
        req.setAttribute("months", getMonths(calendar));
        req.setAttribute("contextPath",req.getContextPath());

        req.getRequestDispatcher("jsps/overview.jsp").forward(req, resp); 
    }


    @Override
    protected String pathQueryToday(Calendar cal){
        return htm.links.overview + queryToday(cal);
    }


    public htm.Node getOverviewTable(List<Job> jobs, String q, String filter){

        Calendar cal = Calendar.getInstance();
        htm.Node table = htm.table().prop("class","overview");

        double tot = 0.0;

        for(Job job: jobs){
            job.addOverviewRow( cal, table, q, handy.isWhiteOrNull(filter) );
            if( job.total != null ){
                tot += job.total;
            }
        }

        if( tot > 0.0 ){
            table.add(htm.tr()
                         .add(htm.td()
                                 .text("Total")
                                 .prop("colspan","5"))
                         .add(htm.td()
                                 .textFmt( "%.2f", tot ))
                         .add(htm.td()));
        }

        return table;
    }
}
