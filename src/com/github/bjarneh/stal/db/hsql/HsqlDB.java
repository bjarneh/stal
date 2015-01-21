// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.db.hsql;

// std
import java.sql.Date;
import java.sql.Types;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import static java.lang.String.format;

// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// local
import com.github.bjarneh.stal.db.DB;
import com.github.bjarneh.stal.types.User;

// libb
import com.github.bjarneh.utilz.globals;


/**
 * An implementation of the DB interface using hsqldb.
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class HsqlDB implements DB {
    
    static final Logger log = Log.getLogger( HsqlDB.class );

    private static String DB_DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    private static String DB_URL    = null;
    private static String DB_USER   = "SA";
    private static String DB_PASS   = "";


    // singleton object
    private static HsqlDB db = null;


    private HsqlDB(){
        ;
    }


    private void init() throws ClassNotFoundException {
        // make sure driver is registered
        Class.forName(DB_DRIVER);
        // this can be set by command line arg or config
        DB_URL = globals.getStr("DB_URL");
        // log some action
        log.info(String.format("HsqlDB.init(%s, %s)", DB_DRIVER, DB_URL ));
    }


    public static DB getDB() throws ClassNotFoundException {
        if( db == null ){
            db = new HsqlDB();
            db.init();
        }
        return db;
    }


    private Connection getConn() throws SQLException {
         return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }



    // start User

    /**
     * {@inheritDoc}
     */
    public User getUserFromPK(String pk)
        throws SQLException
    {
        try(Connection conn = getConn()){
            conn.setReadOnly( true );
            return getUserFromPK( pk, conn );
        }
    }


    private User getUserFromPK(String pk, Connection conn)
        throws SQLException
    {
        User user = null;

        try(PreparedStatement pstmt =
                conn.prepareStatement(SQL.USER_FROM_PK) )
        {

            pstmt.setString(1, pk);
            ResultSet result = pstmt.executeQuery();

            if( result.next() ){
                user = fillUserFromResultset(result);
            }

        }

        return user;
    }


    private User fillUserFromResultset(ResultSet result)
        throws SQLException
    {
        User user = new User();
        user.id   = result.getString("ID");
        user.name = result.getString("NAME");
        return user;
    }



    // end User


    final static class SQL {

        // start User

        final static String USER_FROM_PK =
            "SELECT * FROM USER WHERE ID = ? ";

        // end User
    }
}
