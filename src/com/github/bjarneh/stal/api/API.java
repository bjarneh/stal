// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.api;

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


    private static DB db = null;
    private static API api = null;


    private API(){}


    public static API getAPI(DB impl) throws ClassNotFoundException {
        if( api == null ){
            api = new API();
            if( impl == null ){
                api.db = HsqlDB.getDB();
            }
        }
        return api;
    }


    public static API getAPI() throws ClassNotFoundException {
        return getAPI(null);
    }


    /** Wrapper for. @link{DB#getUserFromPK} */
    public User getUserFromPK(String pk) throws Exception {
        return db.getUserFromPK( pk );
    }


}
