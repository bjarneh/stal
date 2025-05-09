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

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// local
import com.github.bjarneh.stal.api.API;
import com.github.bjarneh.utilz.handy;
import com.github.bjarneh.hour.util.htm;
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.stal.types.Pay;

/**
 * Respond to /stat requests, display statistics.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class StatServlet extends CompanyServlet {

    static final Logger log = Log.getLogger( StatServlet.class );

    /**
     * Initialize objects forwarded to template (jsps/stat.jsp),
     * and forward request.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        req.setAttribute("stats", companyTable());
        req.getRequestDispatcher("jsps/stat.jsp").forward(req, resp); 
    }

    private htm.Node companyTable()
        throws ServletException
    {

        htm.Node table = htm.table();
        HashMap<String, Double> companyMap = companyHours();

        table.add(htm.tr()
                  .add(htm.th().textOk("Company"))
                  .add(htm.th().textOk("Hours")));

        for(String k: companyMap.keySet()){
            table.add(htm.tr()
                         .add(htm.td().text(k))
                         .add(htm.td().textFmt("%.2f",companyMap.get(k))));
        }

        return table;
    }


    private HashMap<String, Double> companyHours()
        throws ServletException
    {
        ArrayList<String> ids = companyIds();

        try{

            HashMap<String, Double> companyMap =
                new HashMap<String, Double>();
            for(String id: ids){
                companyMap.put( id, api.getJobTotalHours(id) );
            }

            return companyMap;

        }catch(Exception e){
            throw new ServletException(e);
        }
    }

}
