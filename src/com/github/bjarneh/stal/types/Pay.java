// Copyright 2014 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.stal.types;

/**
 * Represents a row on the pay table.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public class Pay{

    public String company;
    public String contact;
    public String address;
    public String email;
    public String phone;
    public String assignment;

    //beans!
    public String getCompany(){ return company; }
    public String getContact(){ return contact; }
    public String getAddress(){ return address; }
    public String getEmail(){ return email; }
    public String getPhone(){ return phone; }
    public String getAssignment(){ return assignment; }

}
