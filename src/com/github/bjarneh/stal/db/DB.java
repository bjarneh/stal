// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.db;

// local
import com.github.bjarneh.stal.types.User;

public interface DB {

    /**
     * Return a row from the USER table.
     *
     * @param pk primary key of user
     * @return user object  [row]
     */
    public User getUserFromPK(String pk) throws Exception;

}
