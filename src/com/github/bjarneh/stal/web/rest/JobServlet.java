// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.web.rest;

// std
import java.util.Map;
import java.util.HashMap;
import java.sql.Date;
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
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

// new json api [this will be the standard]
import javax.json.Json;
import javax.json.JsonStructure;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

// libb
import com.github.bjarneh.utilz.handy;
import com.github.bjarneh.utilz.Tuple;

// local
import com.github.bjarneh.stal.api.API;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.web.srv.ApiServlet;



/**
 * Respond to /rest/job requests, create, list, update, delete JOBs.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class JobServlet extends ApiServlet {

    private static final Logger log = Log.getLogger( JobServlet.class );

    static Map<String, Object> config = new HashMap<String, Object>(){{
        put(JsonGenerator.PRETTY_PRINTING, true);
    }};

    private final JsonWriterFactory writerFactory =
            Json.createWriterFactory( config );


    /**
     * Create or update a Job entry.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        JsonReader jsonReader = Json.createReader( req.getReader() );
        JsonObject jsonObj    = jsonReader.readObject();

        //System.out.printf(" jsonObj: %s\n", jsonObj);

        createOrUpdateJob( jsonObj );

        resp.setStatus(resp.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println("ok");

    }


    /**
     * Delete a Job entry.
     */
    @Override
    public void doDelete(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        String[] ids = req.getParameterValues("id");
        String day   = req.getParameter("day");

        if( ids != null && ids.length > 0 ){
            deleteJobs( ids );
        }else if( day != null ){
            deleteDayJobs( day );
        }
        resp.setStatus(resp.SC_OK);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println("ok");
    }


    private void deleteJobs( String[] ids )
        throws ServletException
    {
        try{
            if( ids != null && ids.length > 0 ){
                Job job = new Job();
                for(int i = 0; i < ids.length; i++){
                    job.id = Long.valueOf(ids[i]);
                    api.deleteJob( job );
                }
            }
        }catch(Exception e){
            throw new ServletException(e);
        }
    }


    private void deleteDayJobs( String date )
        throws ServletException
    {
        try{

            Day dayObj = api.getDayFromPK( Date.valueOf( date ) );

            if( dayObj != null && dayObj.jobs != null ){

                for(Job j: dayObj.jobs){
                    api.deleteJob( j );
                }

            }

        }catch(Exception e){
            throw new ServletException(e);
        }
    }


    /**
     * Return Job objects as JSON.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        // debug: see what's going on..
        dumpParams( req, new PrintWriter(System.out, true) );

        String id    = req.getParameter("id");
        String day   = req.getParameter("day");
        String start = req.getParameter("start");
        String stop  = req.getParameter("stop");

        resp.setStatus(resp.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonStructure jstruct = jobSearch(id, day, start, stop);
        JsonWriter jsonWriter = writerFactory.createWriter(resp.getWriter());

        jsonWriter.write(jstruct);
        jsonWriter.close();
    }
    

    private void createOrUpdateJob(JsonObject jsonObj)
        throws ServletException
    {
        try{

            Job job = Job.fromJson( jsonObj );

            if( job.id < 0 ){
                api.createJob( job );
            }else{
                api.updateJob( job );
            }

        }catch(Exception e){
            throw new ServletException(e);
        }

    }


    private JsonStructure jobSearch(
            String id, String day, String start, String stop) 
        throws ServletException
    {
        
        JsonArrayBuilder builder = Json.createArrayBuilder();

        try{

            if( id != null ){

                Job job = api.getJobFromPK(Long.parseLong( id ));

                if( job != null ){
                    return job.toJson();
                }

            }else if( day != null ){

                Day dayObj = api.getDayFromPK( Date.valueOf( day ) );
                
                if( dayObj != null && dayObj.jobs != null ){

                    for(Job j: dayObj.jobs){
                        builder.add(j.toJson());
                    }

                }

            }else if( start != null && stop != null ){

                Day fromDay = Day.fromString( start );
                Day toDay   = Day.fromString( stop );

                ArrayList<Job> jobs = api.intervalJobs( fromDay, toDay );

                for(Job j: jobs){
                    builder.add(j.toJson());
                }

            }

            return builder.build();

        }catch(Exception e){
            throw new ServletException(e);
        }

    }

}
