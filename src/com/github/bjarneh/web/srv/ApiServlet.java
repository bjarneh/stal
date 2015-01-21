// Copyright 2014 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.web.srv;

// stdlib
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;
import java.io.PrintWriter;

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

// local
import com.github.bjarneh.api.API;


/**
 * A super-class for the servlets that need access to the API.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class ApiServlet extends HttpServlet {


    static final Logger log = Log.getLogger( ApiServlet.class );


    protected API api;

    static String dottedLine = 
        "--------------------------------------------------------------------";

    @Override
    public void init() throws ServletException {
        try {
            api = API.getAPI();
        } catch ( ClassNotFoundException e ) {
            throw new ServletException( e );
        }
        log.info("API successfully loaded.... ");
    }


    /**
     * Print parameters given to a HTTP request.
     * @param req is a HTTP request
     * @param writer is where this info is written
     */
    public void dumpParams(HttpServletRequest req, PrintWriter writer)
        throws IOException, ServletException
    {
        writer.println(dottedLine);
        writer.printf(" %s %s\n",
                req.getMethod(), req.getServletPath());
        writer.println(dottedLine);

        int max = 1;

        Map<String, String[]> map = req.getParameterMap();

        for( String k : map.keySet() ){
            if( k.length() > max ){
                max = k.length();
            }
        }

        String tpl = " %"+max+"s = %s\n";

        for( String k : map.keySet() ){
            for( String v : map.get(k) ){
                writer.printf(tpl, k, v );
            }
        }

        writer.println(dottedLine);
    }

}
