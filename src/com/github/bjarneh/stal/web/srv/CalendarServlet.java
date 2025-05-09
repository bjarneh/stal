// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.web.srv;


// std
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// libb
import com.github.bjarneh.utilz.handy;
import com.github.bjarneh.utilz.Tuple;

// local
import com.github.bjarneh.stal.api.API;
import com.github.bjarneh.hour.util.htm;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;


/**
 * Respond to /calendar requests, display year.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class CalendarServlet extends ApiServlet {


    static final Logger log = Log.getLogger( CalendarServlet.class );


    // to be used by parseTimestamp
    static Pattern okHourMinute =
        Pattern.compile("^\\s*(\\d\\d):(\\d\\d)\\s*$");
                

    static final String[] months = {
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    };

    static final String[] strDays = {
        "Mon",
        "Tue",
        "Wed",
        "Thu",
        "Fri",
        "Sat",
        "Sun",
        "" // blank field in header (weeks below)
    };

    static final int[] days = {
        Calendar.MONDAY,
        Calendar.TUESDAY,
        Calendar.WEDNESDAY,
        Calendar.THURSDAY,
        Calendar.FRIDAY,
        Calendar.SATURDAY,
        Calendar.SUNDAY
    };
    


    /**
     * Save a new job entry.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        // debug: see what's going on..
        // dumpParams( req, new PrintWriter(System.out, true) );
        
        // save
        saveDay( dayJobsFromParams(req) );

        // fwd req
        doGet(req, resp);
    }


    /**
     * Initialize objects forwarded to template (jsps/calendar.jsp),
     * and forward request.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {

        Tuple<htm.Node, htm.Node> prevNext;

        Calendar calendar = getCalendar(req);
        Day day = fetchDay(calendar);
        HashMap<Integer, List<Job>> busy = busyDays( calendar );

        req.setAttribute("day", day);

        req.setAttribute("year", calendar.get(Calendar.YEAR));
        prevNext = getPrevNextLinks(calendar);
        req.setAttribute("prev", prevNext.getLeft());
        req.setAttribute("next", prevNext.getRight());

        req.setAttribute("tableMonth", getTableMonth(calendar, busy));

        req.setAttribute("months", getMonths(calendar));
        req.setAttribute("contextPath",req.getContextPath());
        req.setAttribute("postURL", pathQueryToday(calendar));
        req.setAttribute("todayQ", queryToday(calendar));

        req.getRequestDispatcher("jsps/calendar.jsp").forward(req, resp); 
    }


    // TODO(bh) this may not handle leap years very well
    protected Calendar getCalendar(HttpServletRequest req){

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        String year  = req.getParameter("y");
        String day   = req.getParameter("d");

        if( year != null && handy.isInt(year) ){
            cal.set(Calendar.YEAR, Integer.parseInt( year ));
        }
        if( day != null && handy.isInt(day) ){
            cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(day));
        }

        return cal;
    }


    protected String getHumanMonth(Calendar cal){
        return months[cal.get(Calendar.MONTH)];
    }


    protected htm.Node getMonths(Calendar cal){

        Date currentDate = cal.getTime();
        int currentMonth = cal.get(Calendar.MONTH);

        cal.add(Calendar.MONTH, -currentMonth);
        Date start = cal.getTime();

        htm.Node tbody, a, td;

        tbody = htm.tbody();

        for(int i = 0; i < months.length; i++){

            a  = linkToday(cal).textOk(months[i]);
            td = htm.td().add( a );

            if( currentMonth == i ){ td.prop("class","active"); }

            tbody.add(htm.tr().add( td ));

            cal.setTime(start);
            cal.roll(Calendar.MONTH, i+1);

        }

        cal.setTime(currentDate); // reset calendar

        return tbody;
    }


    protected htm.Node linkToday(Calendar cal){
        return htm.a().href(pathQueryToday(cal));
    }


    protected String pathQueryToday(Calendar cal){
        return htm.links.calendar + queryToday(cal);
    }


    protected String queryToday(Calendar cal){
        return "?y="+cal.get(Calendar.YEAR)+"&amp;d="+
                    cal.get(Calendar.DAY_OF_YEAR);
    }


    protected Tuple<htm.Node, htm.Node> getPrevNextLinks(Calendar cal){

        Date now         = cal.getTime();
        int currentYear  = cal.get(Calendar.YEAR);

        htm.Node prev, next;

        cal.set(Calendar.YEAR, currentYear - 1);
        prev = linkToday(cal).text(currentYear - 1)
                             .prop("class","yearlink");

        cal.set(Calendar.YEAR, currentYear + 1);
        next = linkToday(cal).text(currentYear + 1)
                             .prop("class","yearlink")
                             .style("float:right");

        cal.setTime(now); // reset calendar

        return new Tuple<htm.Node, htm.Node>(prev, next);

    }


    private htm.Node getTableMonth(Calendar cal, Map<Integer, List<Job>> busyMap){

        boolean started   = false;
        int count         = 1;
        int currentDay    = cal.get(Calendar.DAY_OF_YEAR);
        int lastDayMonth  = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        Date d = cal.getTime(); // to be able to reset calendar
        HashSet<Integer> busy = new HashSet<Integer>(busyMap.keySet());

        // do not start counting until we hit current month's start day
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int first = cal.get(Calendar.DAY_OF_WEEK);
        int week  = cal.get(Calendar.WEEK_OF_YEAR);

        double total = 0.0;

        htm.Node table = tableHeader();
        htm.Node tr, td, a;

        while(count <= lastDayMonth){
            tr = htm.tr();
            table.add(tr);
            for(int day: days){

                if( (started || day == first) && count <= lastDayMonth ){

                    started = true;
                    a = linkToday(cal).text(count++);

                    if( busy.contains(cal.get(Calendar.DAY_OF_YEAR)) ){
                        a.prop("class","has_hours");
                        List<Job> jobs = busyMap.get(cal.get(Calendar.DAY_OF_YEAR));
                        double dayTotal = 0.0;
                        for(Job j: jobs){
                            j.setTimeUsed();
                            if(j.total != null){
                                total += j.total.doubleValue();
                                dayTotal += j.total.doubleValue();
                            }
                        }
                        a.title(String.format("%.2f", dayTotal));
                    }

                    if( cal.get(Calendar.DAY_OF_YEAR) == currentDay ){
                        a.prop("class","active");
                    }

                    tr.add(htm.td().add( a ));
                    cal.set(Calendar.DAY_OF_MONTH, count);


                }else{
                    tr.add(htm.td());
                }
            }
            tr.add(htm.td().text(week++).prop("class","week"));
            tr.add(htm.td().text(total).prop("class","total"));
            total = 0.0;
        }


        cal.setTime(d); // reset calendar

        return table;
    }


    private htm.Node tableHeader(){

        htm.Node tr    = htm.tr();
        htm.Node table = htm.table().prop("class","month");

        table.add( tr );

        for(String s: strDays){
            tr.add(htm.th().textOk(s));
        }

        return table;
    }


    private Day fetchDay(Calendar cal) throws ServletException {

        Day day;

        try{
            day = api.getDayFromPK(cal);
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }

        if( day == null ){
            day = Day.fromCalendar(cal);
        }

        return day;
    }


    private Day dayJobsFromParams(HttpServletRequest req)
        throws ServletException
    {
        // id => parameter map
        Map<String, Map<String, String>> jobs =
            new HashMap<String, Map<String, String>>();

        // we use underscore (_) as separator, so we find
        // the different days by splitting on _
        String[] tuple;
        String[] params;

        // all parameters, we never use the same parameter twice
        // so all the lists will have one element..
        Map<String, String[]> map = req.getParameterMap();
        for( String k : map.keySet() ){
            params = map.get(k);
            if( params != null && params.length > 0 ){
                if( k.indexOf('_') != -1 ){
                    tuple = k.split("_");
                    if( ! jobs.containsKey( tuple[1] ) ){
                        jobs.put(tuple[1], new HashMap<String,String>());
                    }
                    jobs.get(tuple[1]).put(tuple[0], params[0]);
                }
            }
        }

        //System.out.printf(" %s\n", jobs);

        Calendar calendar = getCalendar(req);
        //System.out.printf(" time: %s\n", calendar.getTime());

        Day day  = Day.fromCalendar(calendar);
        day.jobs = jobsFromMap(day, jobs);

        return day;
    }


    // id = <str('new')|long>
    //
    // id => 
    //   { 
    //     total=<double>,
    //     stop=<hh:mm>,
    //     start=<hh:mm>,
    //     what=<str>,
    //     company=<str>
    //   }
    private ArrayList<Job> 
        jobsFromMap(Day day, Map<String, Map<String, String>> map)
    {

        Job job;
        Map<String,String> jobParams;
        ArrayList<Job> jobs = new ArrayList<Job>();
        boolean delete;

        for(String key: map.keySet()){
            jobParams = map.get(key);
            job = new Job();
            if( handy.isInt(key) ){
                job.id = Long.valueOf(key);
            }// else key.equals( "new" )
            job.dayId   = day.id;
            job.company = jobParams.get("company");
            job.what    = jobParams.get("what");
            job.start   = parseTimestamp(day, jobParams.get("start"));
            job.stop    = parseTimestamp(day, jobParams.get("stop"));
            if( handy.isFloat(jobParams.get("total")) ){
                job.total = handy.toDouble(jobParams.get("total"));
            }
            // delete jobs with 'delete' checkbox checked
            // or if no company name is specified, the
            delete = jobParams.containsKey("delete") &&
                     jobParams.get("delete").equals("on");
            // delete checkbox == no company name == delete
            if( delete ){ job.company = null; }
            if( !handy.isWhiteOrNull(job.company) ){
                jobs.add( job );
            } else {
                // this will delete an old job
                if( job.id != null ){
                    job.id  = job.id * -1;
                    jobs.add( job );
                }
            }
        }

        return jobs;
    }


    protected Timestamp parseTimestamp(Day day, String s){

        int hours, minutes;

        if( s == null ){
            return null;
        }

        Matcher m = okHourMinute.matcher(s);
        if( !m.matches() ){
            return null;
        }else{
            hours    = Integer.parseInt(m.group(1).replaceAll("^0",""));
            minutes  = Integer.parseInt(m.group(2));
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime( day.id );
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);

        cal.add(Calendar.HOUR, hours);
        cal.add(Calendar.MINUTE, minutes);

        return new Timestamp( cal.getTime().getTime() );

    }


    protected ArrayList<Job> jobsThisMonth(Calendar cal, String filter)
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

        //System.out.printf(" %s -> %s \n", first.id, last.id);

        cal.setTime( now );

        try{
            jobs = api.intervalJobs(first, last, filter);
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }

        return jobs;

    }


    private HashMap<Integer, List<Job>> busyDays(Calendar cal)
        throws ServletException
    {
        List<Job> jobs = jobsThisMonth(cal, null);
        HashMap<Integer, List<Job>> busy = new HashMap<>();
        int dayOfYear;

        Date now = cal.getTime();

        if( jobs != null && jobs.size() > 0 ){
            for(Job j: jobs){
                cal.setTime( j.dayId );
                dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
                if(!busy.containsKey(dayOfYear)){
                    busy.put(dayOfYear, new ArrayList<Job>());
                }
                busy.get( dayOfYear ).add( j );
            }
        }

        cal.setTime( now );

        //System.out.printf(" busy: %s\n", busy);

        return busy;
    }


    private void saveDay(Day day) throws ServletException {
        try{
            api.updateDay( day );
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }
    }

}
