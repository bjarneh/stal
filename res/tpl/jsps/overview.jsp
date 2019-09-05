<!DOCTYPE html>
<%@page pageEncoding="utf-8" %>
<%@page contentType="text/html; charset=UTF-8" %>
<html>
    <head>
        <%@include file="header.jspf" %>
    </head>
    <body>

        <h2>
            <a class='nounderline' href='/'>
                Overview ${year}
            </a>
        </h2>

        <%@include file="nav.jspf" %>

        <div class='overviewpane' style="${overviewstyle}">
            ${overview}
        </div>


        <div class='rightpane' style='${divstyle}'>
            <form action='overview${todayQ}' method='POST'>
                <input id='overview_csv' class='savebutton' type='submit' value='CSV' />
                <input type='hidden' name='filter' value='${filter}' />
            </form>
            <table class='months'>
                ${months}
            </table>
            <div class='yearpane'>
                ${prev}
                ${next}
            </div>
        </div>


    </body>

    <script type="text/javascript">
    </script>

</html>
