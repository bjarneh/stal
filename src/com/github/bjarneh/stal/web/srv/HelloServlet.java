// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.web.srv;

// std
import java.io.IOException;
import java.io.PrintWriter;

// servlet api
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// local
import com.github.bjarneh.stal.types.User;


public class HelloServlet extends ApiServlet {


    private String greeting = "Session";


    public HelloServlet(){}


    public HelloServlet(String greeting) {
        this.greeting=greeting;
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        PrintWriter writer = resp.getWriter();
        //req.getRequestDispatcher("mikk.html").forward(req, resp); 
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.println("<h1>"+greeting+"</h1>");
        writer.println("session=" + req.getSession(true).getId());

        dumpParams( req, new PrintWriter(System.out, true) );

        if( req.getParameter("u") != null ){
            User u = getUserFromParams( req );
            if( u != null ){
                writer.println("<dl>");
                writer.println(" <dt> Id </dt>");
                writer.println(" <dd> "+ u.id +"</dd>");
                writer.println(" <dt> Name </dt>");
                writer.println(" <dd> "+ u.name +" </dd>");
                writer.println("</dl>");
            }
        }
    }


    protected User getUserFromParams(HttpServletRequest req)
        throws ServletException
    {
        try{
            
            return api.getUserFromPK( req.getParameter("u") );

        }catch(Exception e){
            throw new ServletException( e );
        }
    }

}
