<!DOCTYPE html>
<%@page pageEncoding="utf-8" %>
<%@page contentType="text/html; charset=UTF-8" %>
<html>
    <head>
        <%@include file="header.jspf" %>
    </head>
    <body>

        <form action='${postURL}' method='POST'>

            <h2>
                <a class='nounderline' href='/'>
                    Calendar ${year}
                </a>
            </h2>

            <div class='wrapper'>

                <div class='leftpane'>
                    ${tableMonth}
                </div>

                <div class='centerpane'>
                    ${day.html}
                </div>
            </div>

            <%@include file="nav.jspf" %>

            <div class='rightpane'>
                <table class='months'>
                    ${months}
                </table>
                <div class='yearpane'>
                    ${prev}
                    ${next}
                </div>
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
