// Copyright 2014 bjarneh@ifi.uio.no. All rights reserved. 
// Use of this source code is governed by a BSD-style 
// license that can be found in the LICENSE file. 

package com.github.bjarneh.hour.util;

// std
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;


// local
/// import com.github.bjarneh.hour.util.handy;
import com.github.bjarneh.utilz.handy;


/**
 * Utility class to format html.
 *
 * @version 1.0
 * @author  bjarneh@ifi.uio.no
 */

public class htm {

    // URIs to servlets; all relative to root, i.e. this
    // web project can be moved away from '/' and still work.

    public static enum links {
        calendar,
        company,
        overview
    }


    static htm instance = null;

    // singleton
    private htm(){};


    public static htm get(){
        if( instance == null ) {
            instance = new htm();
        }
        return instance;
    }


    public static Node div(){
        return get().node("div");
    }

    public static Node span(){
        return get().node("span");
    }

    public static Node pre(){
        return get().node("pre");
    }

    public static Node table(){
        return get().node("table");
    }

    public static Node tbody(){
        return get().node("tbody");
    }

    public static Node tr(){
        return get().node("tr");
    }


    public static Node th(){
        return get().node("th");
    }


    public static Node select(){
        return get().node("select");
    }


    public static Node option(){
        return get().node("option");
    }


    public static Node td(){
        return get().node("td");
    }
    

    public static Node textarea(){
        return get().node("textarea");
    }


    public static Node ol(){
        return get().node("ol");
    }


    public static Node ul(){
        return get().node("ul");
    }


    public static Node li(){
        return get().node("li");
    }


    public static Node input(){
        return get().node("input");
    }


    public static Node a(){
        return get().node("a");
    }


    public static Node a(String path, Map<String,String[]> params){

        Node n = a();

        if( params != null && params.size() > 0 ){
            ArrayList<String> paramList = new ArrayList<String>();
            for( String key: params.keySet() ){
                for( String value: params.get( key ) ){
                    paramList.add( key + "=" + urlEncode( value ) );
                }
            }
            n.href( path + "?" + handy.join("&amp;", paramList) );
        }else{
            n.href( path );
        }

        return n;
    }


    public static Node a(links path, Map<String,String[]> params){
        return a(path.toString(), params);
    }


    // most of the time unique parameters are used
    public static Node anchor(String path, Map<String,String> params){

        Node n = a();

        if( params != null && params.size() > 0 ){
            ArrayList<String> paramList = new ArrayList<String>();
            for( String k: params.keySet() ){
                paramList.add( k + "=" + urlEncode(params.get(k) ));
            }
            n.href( path + "?" + handy.join("&amp;", paramList) );
        }else{
            n.href( path );
        }

        return n;
    }

    public static Node anchor(links path, Map<String,String> params){
        return anchor(path.toString(), params);
    }


    // generic
    public Node node(String tag){
        return new Node(tag);
    }



    /**
     * Convert a map of integers and strings into an html select dropbox.
     *
     * @param name the name of the html select entry
     * @param selected should be your current value or null
     * @param map contains numbers and their human names
     */
    public static Node dropbox(
            String name,
            Integer selected,
            Map<Integer,String> map)
    {

        Node n = select().name(name);

        if( name == null || map == null ){
            return n;
        }

        Node element;
        for( Map.Entry<Integer, String> entry : map.entrySet() ){
            element = htm.option()
                         .value(entry.getKey().toString())
                         .text( entry.getValue() );

            if( entry.getKey() == selected ){
                element.prop("selected", "selected");
            }

            n.add( element );
        }

        return n;
    }


    /**
     * Return a html input checkbox for a Boolean type.
     *
     * @param name the name of the input checkbox
     * @param checked whether it's true or false
     * @return a html checkbox in right state
     */
    public static Node checkbox(String name, Boolean checked){
        Node n = input().name(name).type("checkbox");
        if( checked != null && checked ){
            n.prop("checked", "checked");
        }
        return n;
    }



    // UTF-8 everywhere..
    /**                                                                        
     * URL encode a string and ignore errors.
     * @param s the string we wish to URL encode
     * @return an URL encoded string, or on error the original string
     */     
    public static String urlEncode(String s){  
        try{                      
            if( s == null ){ return null; }
            return URLEncoder.encode(s, "UTF-8");
        }catch(UnsupportedEncodingException e){
            // Nothing we can do.. this should never happen
        }
        return s;                                                              
    }   


    /**
     * Escape HTML semi-illegal characters from String.
     *
     * @param s the string to convert into safe html.
     * @return a string without &gt; &lt; &quot; ' /
     */
    public static String htmlEscape(String s){
        if( s == null ){ return null; }
        StringBuilder sb = new StringBuilder();
        char[] letters   = s.toCharArray();
        for(int i = 0; i < letters.length; i++){
            switch( letters[i] ){
                case '&' : sb.append("&amp;");  break;
                case '<' : sb.append("&lt;");   break;
                case '>' : sb.append("&gt;");   break;
                case '"' : sb.append("&quot;"); break;
                case '\'': sb.append("&#x27;"); break;
                case '/' : sb.append("&#x2F;"); break;
                default  : sb.append(letters[i]);
            }
        }
        return sb.toString();
    }



    public class Node {


        String nodeType  = null; // td,body,ul, etc..
        String content   = "";


        ArrayList<Node> children = null;
        HashMap<String, String> props = null;


        public Node(String type){
            nodeType = type;
        }


        public ArrayList<Node> getChildren(){
            return children;
        }


        public Node id(String s){
            return prop("id", s);
        }


        public Node type(String s){
            return prop("type", s);
        }


        public Node title(String s){
            return prop("title", s);
        }


        public Node name(String s){
            return prop("name", s);
        }


        public Node value(Object s){
            return propEsc("value", s);
        }


        public Node style(String s){
            return prop("style", s);
        }


        public Node href(String s){
            return prop("href", s);
        }


        public Node text(Object o){
            if( o != null ){
                text(o.toString());
            }
            return this;
        }


        public Node text(String s){
            if( s != null ){
                content = htmlEscape(s);
            }
            return this;
        }


        public Node text(Double v){
            if( v != null ){
                textOk(v.toString());
            }
            return this;
        }


        public Node text(Integer v){
            if( v != null ){
                textOk(v.toString());
            }
            return this;
        }


        // a method which does not escape html tags
        public Node textOk(String s){
            content = s;
            return this;
        }


        public Node textFmt(String fmt, Object ... args ){
            content = String.format(fmt, args);
            return this;
        }


        public Node add(Node n){
            if( children == null ){
                children = new ArrayList<Node>();
            }
            children.add(n);
            return this;
        }

        
        public Node add(Collection<Node> coll){
            if( children == null ){
                children = new ArrayList<Node>();
            }
            children.addAll(coll);
            return this;
        }


        public Node prop(String k, String v){
            if( props == null ){
                props = new HashMap<String, String>();
            }
            props.put(k,v);
            return this;
        }


        public Node prop(String k, Object v){
            if( v != null ){
                prop(k, v.toString());
            }
            return this;
        }


        public Node propEsc(String k, String v){
            return prop(k, htmlEscape(v));
        }


        public Node propEsc(String k, Object v){
            if( v != null ){
                propEsc(k, v.toString());
            }
            return this;
        }


        private String propStr(){
            String s = "";
            if( props != null ){
                for( String k : props.keySet() ){
                    s = s.concat(" ")
                         .concat(k)
                         .concat("=")
                         .concat("\"")
                         .concat( props.get(k) )
                         .concat("\"");
                }
            }
            return s;
        }


        private void propStr(StringBuilder sb){
            if( props != null ){
                for( String k : props.keySet() ){
                    sb.append(" ")
                      .append(k)
                      .append("=")
                      .append("\"")
                      .append( props.get(k) )
                      .append("\"");
                }
            }
        }


        @Override
        public String toString(){

            String tmp = "<".concat(nodeType)
                            .concat(propStr())
                            .concat(">");

            if( children != null ){
                for(Node c: children){
                    tmp = tmp.concat(c.toString());
                }
            }

            tmp = tmp.concat(content)
                     .concat("</")
                     .concat(nodeType)
                     .concat(">");

            return tmp;
        }


        public void toString(StringBuilder sb){

            sb.append("<").append(nodeType);
            propStr(sb); // appends properties
            sb.append(">");
            sb.append(content);

            if( children != null ){
                for(Node c: children){
                    c.toString( sb );
                }
            }

            sb//.append(content)
              .append("</")
              .append(nodeType)
              .append(">");
        }


        public String str(){
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }


        public String childStr(){
            StringBuilder sb = new StringBuilder();
            for( Node n: children ){
                n.toString(sb);
            }
            return sb.toString();
        }

    }
}
