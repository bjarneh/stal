// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.api;


// jetty
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

// local
import com.github.bjarneh.stal.db.DB;
import com.github.bjarneh.stal.db.hsql.HsqlDB;
import com.github.bjarneh.stal.types.User;

/**
 * Singleton that wraps a DB implementation to allow injection.
 *
 * @version 1.0
 * @author bjarneh@ifi.uio.no
 */

public class API {

    static final Logger log = Log.getLogger(API.class);

    private static DB db = null;
    private static API api = null;


    private API(){}


    public static API getAPI(DB impl) throws Exception {
        if( api == null ){
            api = new API();
            if( impl == null ){
                api.db = HsqlDB.getDB();
            }
            log.info("API loaded.... ");
        }
        return api;
    }


    public static API getAPI() throws Exception {
        return getAPI(null);
    }


    /** Wrapper for. @link{DB#getUserFromPK} */
    public User getUserFromPK(String pk) throws Exception {
        return db.getUserFromPK( pk );
    }


}
