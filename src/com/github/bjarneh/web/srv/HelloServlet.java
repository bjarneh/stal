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

        //req.getRequestDispatcher("mikk.html").forward(req, resp); 
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().println("<h1>"+greeting+"</h1>");
        resp.getWriter().println("session=" + req.getSession(true).getId());

        resp.getWriter().println("<pre>");
        dumpParams( req, resp.getWriter() );
        resp.getWriter().println("</pre>");
    }

}
