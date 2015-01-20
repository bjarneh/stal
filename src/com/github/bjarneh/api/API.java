// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.api;

import com.github.bjarneh.db.DB;

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

    public static API getAPI() {
        if( api == null ){
            api = new API();
        }
        return api;
    }

}
