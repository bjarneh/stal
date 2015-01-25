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
import com.github.bjarneh.stal.types.Day;
import com.github.bjarneh.stal.types.Job;
import com.github.bjarneh.stal.types.Pay;

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Respond to /company requests, display company info etc..
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class CompanyServlet extends ApiServlet {

///     private static final Logger log =
///         Logger.getLogger( CompanyServlet.class.getName() );

    /**
     * Update company info.
     */
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        createOrUpdateCompany(req);

        doGet(req, resp);
    }


    /**
     * Initialize objects forwarded to template (jsps/company.jsp),
     * and forward request.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException, ServletException
    {
        String id = req.getParameter("id");
        ArrayList<String> ids = companyIds();
        
        if( handy.isWhiteOrNull(id) && ids != null && ids.size() > 0 ){
            id = ids.get(0);
        }

        req.setAttribute("id", htm.htmlEscape(id));
        req.setAttribute("pay", getCompany(id));
        req.setAttribute("companies", companyTable(id, ids));
        req.setAttribute("contextPath",req.getContextPath());
        req.getRequestDispatcher("jsps/company.jsp").forward(req, resp); 

    }


    private Pay getCompany(String id)
        throws ServletException
    {
        try{
            return api.getPayFromPK(id);
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }
    }


    private ArrayList<String> companyIds()
        throws ServletException
    {
        try{
            return api.getCompanyNames();
        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }
    }


    private htm.Node companyTable(String id, List<String> ids) {

        htm.Node a, table = null;

        table = htm.table().prop("class","companies");

        if( ids != null && ids.size() > 0 ){
            for(String name: ids){
                a = htm.a().text(name);
                a.href(htm.links.company + "?id=" +
                        htm.urlEncode(name));
                if( id != null && id.equals(name) ){
                    a.prop("class","active");
                }
                table.add(htm.tr().add(htm.td().add(a)));
            }
        }

        return table;
    }


    private void createOrUpdateCompany(HttpServletRequest req)
        throws ServletException
    {
        try{

            String id;
            Pay pay, current;

            pay = payFromParams( req );

            if( pay != null ){
                current = api.getPayFromPK(pay.company);
                if( current != null ){
                    api.updatePay( pay );
                }else{
                    api.createPay( pay );
                }

            }else{
                id = req.getParameter("id");
                current = api.getPayFromPK(id);
                if( current != null ){
                    System.out.printf(" delete? [%s]\n", id);
                }
            }

        }catch(Exception e){
            throw new ServletException(e.getMessage(), e);
        }

    }


    private Pay payFromParams(HttpServletRequest req){

        Pay p = new Pay();
        
        String c = req.getParameter("name");
        if( !handy.isWhiteOrNull(c) ){
            p.company = c;
        }else{
            return null;
        }

        p.contact    = req.getParameter("contact");
        p.address    = req.getParameter("address");
        p.address    = req.getParameter("address");
        p.email      = req.getParameter("email");
        p.phone      = req.getParameter("phone");
        p.assignment = req.getParameter("assignment");

        return p;

    }

}
