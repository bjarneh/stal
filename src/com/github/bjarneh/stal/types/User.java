// Copyright 2015 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.types;

// Row in the USER table
public class User {

    public String id;
    public String name;

    @Override
    public String toString(){
        return String.format("User{%s, %s}", id, name);
    }
}
