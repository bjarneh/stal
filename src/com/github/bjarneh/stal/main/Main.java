// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.main;

// std
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import javax.servlet.Servlet;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

// jetty
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.jsp.JettyJspServlet;

// apache
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;

// libb
import com.github.bjarneh.utilz.io;
import com.github.bjarneh.utilz.res;
import com.github.bjarneh.utilz.path;
import com.github.bjarneh.utilz.globals;
import com.github.bjarneh.parse.options.Getopt;

// local
import com.github.bjarneh.web.srv.HelloServlet;
import com.github.bjarneh.web.srv.CompanyServlet;
import com.github.bjarneh.web.srv.CalendarServlet;
import com.github.bjarneh.web.srv.OverviewServlet;
import com.github.bjarneh.web.rest.JobServlet;

/**
 * Entry point for the embedded Jetty web-app.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public class Main {

    final static Logger log = Log.getLogger(Main.class);

    // If we cannot find this, we must manipulate ClassLoader
    final static String handyClass = "com.github.bjarneh.utilz.handy";

    final static String version = "stal v1.0";

    final static String help = 
        "                                                           \n"+
        " stal - trying out Jetty [embedded]                        \n"+
        "                                                           \n"+
        " usage: stal [OPTIONS]                                     \n"+
        "                                                           \n"+
        " options:                                                  \n"+
        "  -h  --help    : print this menu and exit                 \n"+
        "  -v  --version : print version and exit                   \n"+
        "  -s  --store   : store [default:$HOME/.stal/store/db]     \n"+
        "  -t  --tmp     : jsp compile dir [default:/tmp/stal/jsp]  \n"+
        "  -j  --jvm     : jsp jvm compile/target [default:1.7]     \n"+
        "  -r  --root    : set webroot [default:built-in]           \n"+
        "  -p  --port    : set port number [default:7676]           \n";


    // all boolean flags start out false
    static HashMap<String, Boolean> boolMap = new HashMap<String, Boolean>(){{
        put("-help", false);
        put("-version", false);
    }};

    // all string options have a default as well
    static HashMap<String, String> strMap = new HashMap<String, String>(){{
        put("-root", null);
        put("-port", "7676");
        put("-jvm", "1.7");
        put("-tmp", 
                path.join(System.getProperty("java.io.tmpdir"), 
                                 path.fromSlash("stal/jsp")));
        put("-store", 
                path.join(System.getProperty("user.home"), 
                                 path.fromSlash(".stal/store/db")));
    }};


    // MIME overrides
    static HashMap<String, String> mimeMap = new HashMap<String, String>(){{
        put("txt","text/plain; charset=UTF-8");
        put("html","text/html; charset=UTF-8");
        put("java","text/plain; charset=UTF-8");
        put("js","application/javascript; charset=UTF-8");
    }};


    // Servlets
    // PATH => Servlet [without the d, i.e. d/hello => hello servlet]
    static HashMap<String, Servlet> srvMap = new HashMap<String, Servlet>(){{
        // GUI
        put("/hello", new HelloServlet());
        put("/calendar", new CalendarServlet());
        put("/overview", new OverviewServlet());
        put("/company", new CompanyServlet());
        // REST API
        put("/rest/job", new JobServlet());
    }};


    // command line parser [ not actual GNU-getopt ]
    static Getopt getopt = null;



    // where to look for JSP libraries, we are usually self-contained
    static String packedRegex = ".*stal$";

    // all dependencies could be given as classpath arguments on startup
    static String fancyRexex = 
                ".*/[^/]*servlet-api-[^/]*\\.jar$|"+
                ".*/javax.servlet.jsp.jstl-.*\\.jar$|"+
                ".*/.*taglibs.*\\.jar$";

    // this is the default, @see getWorkingClassLoader
    static String jarRegex = packedRegex;


    static void initParser() {

        getopt = new Getopt();

        getopt.addBoolOption("-h -help --help help");
        getopt.addBoolOption("-v -version --version");
        getopt.addFancyStrOption("-s --store");
        getopt.addFancyStrOption("-t --tmp");
        getopt.addFancyStrOption("-j --jvm");
        getopt.addFancyStrOption("-r --root");
        getopt.addFancyStrOption("-p --port");

    }


    static String[] parseArgs(String[] args) {

        String[] rest = getopt.parse( args );

        for(String k: boolMap.keySet()){
            if( getopt.isSet(k) ){
                boolMap.put(k, true);
            }
        }

        for(String k: strMap.keySet()){
            if( getopt.isSet(k) ){
                strMap.put(k, getopt.get(k));
            }
        }

        // to be used again by new config file etc.
        getopt.reset(); 

        return rest;

    }


    static void parseConf() throws IOException {

        String home = System.getProperty("user.home");
        String pwd  = System.getProperty("user.dir");

        ArrayList<String> configs = new ArrayList<String>();

        // in order of importance
        configs.add( path.join(home, ".stalrc") );
        configs.add( path.join(home, path.fromSlash(".config/stal/stalrc")) );

        configs.add( path.join(pwd, "_stalrc") );
        configs.add( path.join(pwd, ".stalrc") );
        configs.add( path.join(pwd, "stalrc") );

        for( String c: configs ){
            if( path.isFile( c ) ){
                parseArgs( argsFromConfig( c ) );
            }
        }

    }


    /**
     * Read command line arguments from a file, ignoring
     * lines that start with a #-sign.
     *
     * @param fname a file-name to read from
     * @return a command line arguments read from file
     */
    static String[] argsFromConfig( String fname )
        throws IOException
    {
        byte[] raw = io.raw( fname );

        if( raw != null && raw.length > 0 ){

            ArrayList<String> args = new ArrayList<String>();
            String[] lines = new String(raw).split("\n");

            for(String line: lines){
                if( ! line.matches("^\\s*#.*$") ) {
                    for(String token: line.split("\\s+")){
                        if( token.length() > 0 ){
                            args.add( token );
                        }
                    }
                }
            }

            return args.toArray( new String[args.size()] );
        }

        return null;
    }


    /**
     * Create a handler for static web content.
     *
     * @param path incoming path for static files, typically slash.
     * @param dirListing should we allow listing of static content
     * @param webroot where to look for static content
     *
     * @return a handler for static content
     */
    private static Handler getFixedHandler(
            String path, boolean dirListing, String webroot ) 
    {
        ContextHandler fixedContext = new ContextHandler();
        fixedContext.setContextPath( path );

        ResourceHandler fixedHandler = new ResourceHandler();
        fixedHandler.setDirectoriesListed( dirListing );

        // Add alternative MIME types
        if( mimeMap.size() > 0 ){
            MimeTypes mimeTypes = fixedHandler.getMimeTypes();
            for(String k: mimeMap.keySet()){
                mimeTypes.addMimeMapping(k, mimeMap.get(k));
            }
        }

        if( webroot == null ){
            fixedHandler.setResourceBase(
                    res.get().url("s/").toExternalForm() );
        }else{
            fixedHandler.setResourceBase( webroot );
        }

        fixedContext.setHandler( fixedHandler );

        return fixedContext;

    }


    /**
     * WUT 1: Ensure the jsp engine is initialized correctly.
     */
    private static ArrayList<ContainerInitializer> jspInitializers() {

        JettyJasperInitializer sci       = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);

        ArrayList<ContainerInitializer> initializers = 
            new ArrayList<ContainerInitializer>();
        initializers.add(initializer);

        return initializers;

    }

    
    /**
     * WUT 2: Create JSP Servlet (must be named "jsp").
     */
    private static ServletHolder jspServletHolder() {

        ServletHolder jspHolder = 
            new ServletHolder("jsp", JettyJspServlet.class);

        jspHolder.setInitOrder(0);
        jspHolder.setInitParameter("logVerbosityLevel", "DEBUG");
        jspHolder.setInitParameter("fork", "false");
        jspHolder.setInitParameter("xpoweredBy", "false");
        jspHolder.setInitParameter("compilerTargetVM", strMap.get("-jvm"));
        jspHolder.setInitParameter("compilerSourceVM", strMap.get("-jvm"));
        jspHolder.setInitParameter("keepgenerated", "true");

        return jspHolder;

    }


    /**
     * WUT 3: Create Default Servlet (must be named "default").
     *
     * TODO FIXME use this or ResourceHandler, i.e. this default
     * servlet can be used to serve static content, and can also
     * be used with filters, i.e. ResourceHandler cannot be filtered.
     */
    private static ServletHolder defaultServletHolder(URI baseUri) {

        ServletHolder holderDefault = 
            new ServletHolder("default", DefaultServlet.class);
        holderDefault.setInitParameter(
                "resourceBase", baseUri.toASCIIString());
        holderDefault.setInitParameter("dirAllowed", "true");

        return holderDefault;

    }


    // 1. Find handyClass, or give up.
    // 2. If we only have 1 URL in the class-path we investigate MANIFEST
    //    to find new URLs to add to class-loader.
    // 3. If we have many URLs in the class-path we should update the
    //    ContainerIncludeJarPattern to reflect this (fancyRexex).
    private static ClassLoader getWorkingClassLoader() 
        throws ClassNotFoundException, IOException
    {

        Class clazz;
        ClassLoader classLoader = new Main().getClass().getClassLoader();

        if( classLoader instanceof URLClassLoader ){

            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;

            // this will fail if libraries are not located
            urlClassLoader.loadClass( handyClass );

            // we are ok, jars are given as command line arguments
            if( urlClassLoader.getURLs().length > 1 ){

                jarRegex = fancyRexex;
                return urlClassLoader;

            }else{

                URL url = res.get().url(path.join("META-INF","MANIFEST.MF"));
                Manifest manifest = new Manifest(url.openStream());

                Attributes attributes = manifest.getMainAttributes();
                String classPath = attributes.getValue("Class-Path");

                if( classPath != null && classPath.length() > 0 ){

                    String[] classPaths = classPath.split("\\s+");

                    if( classPaths.length > 0 ){

                        URL[] urls = new URL[ classPaths.length ];

                        for(int i = 0; i < classPaths.length; i++){
                            urls[i] = new URL( "file:"+ classPaths[i] );
                        }

                        log.info("Manifest Class-Path added!");

                        jarRegex = fancyRexex;
                        return new URLClassLoader(urls, classLoader); 
                    }
                }
            }
        }

        return classLoader;
    }


    private static Handler getDynamicHandler( String path, String tmpDir )
        throws IOException, URISyntaxException, ClassNotFoundException
    {
        WebAppContext dynCtx = new WebAppContext();

        dynCtx.setContextPath( path );
        dynCtx.setResourceBase( res.get().url("tpl/").toExternalForm() );
        dynCtx.setAttribute("javax.servlet.context.tempdir", new File(tmpDir));

        // WUT 4
        dynCtx.setAttribute(
            "org.eclipse.jetty.containerInitializers", jspInitializers());
        // WUT 5
        dynCtx.setAttribute(InstanceManager.class.getName(), 
                            new SimpleInstanceManager());
        // WUT 6
        dynCtx.addBean(new ServletContainerInitializersStarter(dynCtx), true);

        // WUT 7 
        dynCtx.setClassLoader(
                new WebAppClassLoader(
                    getWorkingClassLoader(), dynCtx));

        // NOTE: jarRegex is updated by getWorkingClassLoader,
        //       i.e. this has to be called after getWorkingClassLoader
        dynCtx.setAttribute(
                "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                jarRegex );

        log.info( "jarRegex: "+ jarRegex );

        dynCtx.addServlet(jspServletHolder(), "*.jsp");

        // Add mapping to servlets from map
        for(String k: srvMap.keySet()){
            dynCtx.addServlet( new ServletHolder(srvMap.get(k)), k );
        }

        return dynCtx;
    }


    /**
     * Entry point, parse config + arguments and start up server.
     * @param args command line arguments
     */
    public static void main(String[] args) throws Exception {

        initParser();
        parseConf();

        String[] rest = parseArgs( args );

        if( boolMap.get("-help") ){
            System.out.println(help); System.exit(0);
        }
        if( boolMap.get("-version") ){
            System.out.println(version); System.exit(0);
        }

        // allow root to be set without '-r/root' flag
        if( rest != null && rest.length > 0 ){
            strMap.put("-root", rest[0]);
        }

        // set dir to file store
        globals.set("DB_URL", "jdbc:hsqldb:file:"+strMap.get("-store"));

        if( ! path.isDir(strMap.get("-store")) ) {
            new File(strMap.get("-store")).mkdirs();
            log.info("Created dir: "+strMap.get("-store"));
        }

        // Initialize new server object
        Server server = new Server( Integer.parseInt(strMap.get("-port")) );

        // Static content.
        Handler fixedHandler = getFixedHandler("/", true, strMap.get("-root"));
        // Dynamic content
        Handler dynamicHandler = getDynamicHandler("/d", strMap.get("-tmp"));

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { dynamicHandler, fixedHandler });
 
        server.setHandler( contexts );

        server.start();
        server.join();

    }

}
