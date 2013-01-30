<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="reportPages" type="java.util.HashMap" scope="request"/>

<meta charset="utf-8"/>

<div id='mainContainer'>
    <div class="simpleTabs">
        <ul class="simpleTabsNavigation">
            <c:forEach var="item" items="${reportPages}">
                <li><a href="#">${item.key}</a></li>
            </c:forEach>
        </ul>

        <c:forEach var="item" items="${reportPages}">
            <div class="simpleTabsContent">
                    ${item.value}
            </div>
        </c:forEach>
    </div>
</div>

<style type="text/css">
    div.simpleTabs {
        padding: 10px;
    }

    ul.simpleTabsNavigation {
        margin: 0 10px;
        padding: 0;
        text-align: left;
    }

    ul.simpleTabsNavigation li {
        list-style: none;
        display: inline;
        margin: 0;
        padding: 0;
    }

    ul.simpleTabsNavigation li a {
        border: 1px solid #E0E0E0;
        padding: 6px 6px;
        background: #F0F0F0;
        font-size: 14px;
        text-decoration: none;
        font-family: Georgia, "Times New Roman", Times, serif;
    }

    ul.simpleTabsNavigation li a:hover {
        background-color: #F6F6F6;
    }

    ul.simpleTabsNavigation li a.current {
        background: #fff;
        color: #222;
        border-bottom: 1px solid #fff;
    }

    div.simpleTabsContent {
        border: 1px solid #E0E0E0;
        padding: 5px 15px 15px;
        margin-top: 3px;
        display: none;
    }

    div.simpleTabsContent.currentTab {
        display: block;
    }

</style>