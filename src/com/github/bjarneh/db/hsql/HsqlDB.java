package com.github.bjarneh.db.hsql;


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
import com.github.bjarneh.db.DB;

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

}
