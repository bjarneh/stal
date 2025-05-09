// Copyright 2014 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.types;

// std
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import static java.lang.String.format;

// local
import com.github.bjarneh.hour.util.htm;

// json
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonStructure;

/**
 * Represents a row on the job table.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public class Job {

    public Long id;
    public Date dayId;
    public String company;
    public Timestamp start;
    public Timestamp stop;
    public Double total;
    public String what;

    static final String[] WEEK_DAYS = {
        "",  // No day in Calendar is 0
        "Su",
        "Mo",
        "Tu",
        "We",
        "Th",
        "Fr",
        "Sa",
    };

    static final long MINUTE_MILLIS = 1000 * 60;
    static final long HOUR_MILLIS   = MINUTE_MILLIS * 60;
    static final long DAY_MILLIS    = 24 * HOUR_MILLIS;

    static String startDefault = "09:00";
    static String stopDefault  = "17:00";


    public void addInputRows(htm.Node table){

        String uid = (id == null)? "new" : ""+id;

        htm.Node tr, input;
        tr = htm.tr();
        // delete toggle
        input = htm.input()
                   .type("checkbox")
                   .name("delete_"+uid);
        tr.add(htm.td().add( input ));
        // company
        input = htm.input()
                   .type("text")
                   .name("company_"+uid)
                   .title("Company [blank = delete]")
                   .prop("placeholder","Company [blank = delete]")
                   .prop("size","28")
                   .value(company);
        tr.add(htm.td().add( input ));
        // start
        input = htm.input()
                   .type("text")
                   .name("start_"+uid)
                   .title("Start (hh:mm)")
                   .prop("size","4")
                   .value(fmtTime(start));
        if( id == null ){ input.value(startDefault); }
        tr.add(htm.td().add( input ));
        // stop
        input = htm.input()
                   .type("text")
                   .name("stop_"+uid)
                   .title("Stop (hh:mm)")
                   .prop("size","4")
                   .value(fmtTime(stop));
        if( id == null ){ input.value(stopDefault); }
        tr.add(htm.td().add( input ));
        // total
        input = htm.input()
                   .type("text")
                   .name("total_"+uid)
                   .prop("size","3")
                   .title("Total hours spent (decimal number)")
                   .prop("class","rightalign")
                   .value(total);
        tr.add(htm.td().add( input ));
        table.add( tr );

        // what
        tr = htm.tr();
        input = htm.textarea()
                   .name("what_"+uid)
                   .title("Short description of what you did")
                   .text(what);
        tr.add(htm.td()
                  .prop("colspan","5")
                  .add( input ));
        table.add( tr );
    }


    public static String fmtTime(Timestamp t){

        if( t == null ){ return null; }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( t.getTime() );

        int hours   = cal.get(Calendar.HOUR);
        int minutes = cal.get(Calendar.MINUTE);

        if( cal.get(Calendar.AM_PM) == Calendar.PM ){
            hours += 12;
        }

        return String.format("%02d:%02d", hours, minutes);
    }


    public static void addBlankRows(htm.Node n){
        new Job().addInputRows(n);
    }


    public void addOverviewRow(
        Calendar cal, htm.Node table, String q, boolean addFilter)
    {

        //Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayId.getTime());

        int year = cal.get(Calendar.YEAR);
        int day  = cal.get(Calendar.DAY_OF_MONTH);
        int wday = cal.get(Calendar.DAY_OF_WEEK);
        int yday = cal.get(Calendar.DAY_OF_YEAR);

        if( addFilter ){
            q += "&amp;filter="+ htm.urlEncode( company );
        }
        table.add(htm.tr()
                     .add(htm.th()
                             .add(htm.a()
                                 .prop("class","discrete")
                                 .href("/d/calendar?y="+year+"&amp;d="+yday)
                                 .text(day)))
                     .add(htm.td().prop("class","mono")
                                  .text(WEEK_DAYS[wday]))
                     .add(htm.td()
                             .add(htm.a()
                                     .href(q)
                                     .text(company)))
                     .add(htm.td().text(fmtTime(start)))
                     .add(htm.td().text(fmtTime(stop)))
                     .add(htm.td()
                             .prop("class","num")
                             .text(total))
                     .add(htm.td().text(what)));
    }


    public void setTimeUsed(){
        if( total == null && start != null && stop != null ){
            long used = stop.getTime() - start.getTime();
            if( used < 0 ){
                used = (stop.getTime() + DAY_MILLIS) - start.getTime();
            }
            if( used > 0 ){
                long minutes = (used%HOUR_MILLIS)/MINUTE_MILLIS;
                double minAsPercent = (double) minutes/60.0;
                long minutesOk      = (long)  (minAsPercent*100);
                total = Double.valueOf(format("%d.%d",
                            (used/HOUR_MILLIS), minutesOk));
            }
        }
    }


    public static Job fromJson(JsonObject jsonObj)
        throws Exception
    {
        Job j     = new Job();

        j.id      = jsonObj.getJsonNumber("id").longValue();
        j.dayId   = Date.valueOf(jsonObj.getString("dayId"));
        j.company = jsonObj.getString("company");
        j.start   = Timestamp.valueOf(jsonObj.getString("start"));
        j.stop    = Timestamp.valueOf(jsonObj.getString("stop"));
        if( jsonObj.containsKey("total") ){
            j.total   = jsonObj.getJsonNumber("total").doubleValue();
        }
        j.what    = jsonObj.getString("what");

        return j;
    }


    public JsonStructure toJson(){
        return Json.createObjectBuilder()
                   .add("id", id)
                   .add("dayId", dayId.toString())
                   .add("company", company)
                   .add("start", start.toString())
                   .add("stop", stop.toString())
                   .add("total", total)
                   .add("what", what)
                   .build();
    }


    @Override
    public String toString(){
        return toJson().toString();
    }

}
