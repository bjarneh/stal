<!DOCTYPE html>
<%@page pageEncoding="utf-8" %>
<%@page contentType="text/html; charset=UTF-8" %>
<html>
    <head>
        <%@include file="header.jspf" %>
    </head>
    <body>

        <h2>
            <a class='nounderline' href='${contextPath}'>
                Overview ${year}
            </a>
        </h2>

        <%@include file="nav.jspf" %>

        <div class='overviewpane'>
            ${overview}
        </div>

        <div class='rightpane'>
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
