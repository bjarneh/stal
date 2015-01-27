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
import javax.json.JsonArray;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
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
        // fwd req
        doGet(req, resp);
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

        String id  = req.getParameter("id");
        String day = req.getParameter("day");
        String max = req.getParameter("max");

        resp.setStatus(resp.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonStructure jstruct = jobSearch(id, day, max);
        JsonWriter jsonWriter = writerFactory.createWriter(resp.getWriter());

        jsonWriter.write(jstruct);
        jsonWriter.close();
    }


    private JsonStructure jobSearch(String id, String day, String max) 
        throws ServletException
    {

        try{

            if( id != null ){

                Job job = api.getJobFromPK(Long.parseLong( id ));

                if( job != null ){
                    return job.toJson();
                }

            }else if( day != null ){

                Day dayObj = api.getDayFromPK( Date.valueOf( day ) );
                
                if( dayObj != null && dayObj.jobs != null ){

                    JsonArrayBuilder builder = Json.createArrayBuilder();

                    for(Job j: dayObj.jobs){
                        builder.add(j.toJson());
                    }

                    return builder.build();
                }

            }

            return Json.createArrayBuilder().build();

        }catch(Exception e){
            throw new ServletException(e);
        }

    }

}
