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


    static final long MINUTE_MILLIS = 1000 * 60;
    static final long HOUR_MILLIS   = MINUTE_MILLIS * 60;

    static String startDefault = "09:00";
    static String stopDefault  = "17:00";


    public void addInputRows(htm.Node table){

        String uid = (id == null)? "new" : ""+id;

        htm.Node tr, input;
        tr = htm.tr();
        // company
        input = htm.input()
                   .type("text")
                   .name("company_"+uid)
                   .title("Company name (unique)")
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
                  .prop("colspan","4")
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


    public void addOverviewRow(htm.Node table){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dayId.getTime());
        int day = cal.get(Calendar.DAY_OF_MONTH);
        table.add(htm.tr()
                     .add(htm.th().text(day))
                     .add(htm.td().text(company))
                     .add(htm.td().text(fmtTime(start)))
                     .add(htm.td().text(fmtTime(stop)))
                     .add(htm.td().text(total))
                     .add(htm.td().text(what)));
    }


    public void setTimeUsed(){
        if( total == null && start != null && stop != null ){
            long used = stop.getTime() - start.getTime();
            if( used > 0 ){
                long minutes = (used%HOUR_MILLIS)/MINUTE_MILLIS;
                double minAsPercent = (double) minutes/60.0;
                long minutesOk      = (long)  (minAsPercent*10);
                total = new Double(format("%d.%d",
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
