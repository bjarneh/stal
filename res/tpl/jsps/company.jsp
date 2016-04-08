<!DOCTYPE html>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> 
<%@ page pageEncoding="utf-8" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<html>
    <head>
        <%@include file="header.jspf" %>
    </head>
    <body>

        <form action='company' method='POST'>

            <input type='hidden' name='id' value='${id}' />

            <h2>
                <a class='nounderline' href='/'>
                    Company
                </a>
            </h2>


            <%@include file="nav.jspf" %>


            <div class='companyleft'>
                ${companies}
            </div>

            
            <div class='companyright'>
                <table class='company'>
                    <tr>
                        <th>Company</th>
                        <td>
                            <input type='text'
                                   name='name'
                                   value='<c:out value="${pay.company}" />' />
                        </td>
                    </tr>
                    <tr>
                        <th>Contact</th>
                        <td>
                            <input type='text'
                                   name='contact'
                                   value='<c:out value="${pay.contact}" />' />
                        </td>
                    </tr>
                    <tr>
                        <th>Email</th>
                        <td>
                            <input type='text'
                                   name='email'
                                   value='<c:out value="${pay.email}" />' />
                        </td>
                    </tr>
                    <tr>
                        <th>Phone</th>
                        <td>
                            <input type='text'
                                   name='phone'
                                   value='<c:out value="${pay.phone}" />' />
                        </td>
                    </tr>
                    <tr>
                        <th>Address</th>
                        <td>
                            <input type='text'
                                   name='address'
                                   value='<c:out value="${pay.address}" />' />
                        </td>
                    </tr>
                    <tr>
                        <th style='vertical-align: top;'>Assignment</th>
                        <td>
                            <textarea
                                name='assignment'><c:out value="${pay.assignment}" /></textarea>
                        </td>
                    </tr>
                </table>
            </div>


            <div class='bottomleftpane'>
                <input type='submit'
                       name='Save'
                       class='savebutton'
                       value='Save' />
            </div>

        </form>

    </body>

    <script type="text/javascript">
    </script>

</html>
