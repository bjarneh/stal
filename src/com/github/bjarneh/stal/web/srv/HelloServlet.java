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
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.println("<h1>"+greeting+"</h1>");
        writer.println("session=" + req.getSession(true).getId());

    }

}
