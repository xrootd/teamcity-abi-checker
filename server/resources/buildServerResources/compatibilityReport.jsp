<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="reportPages" type="java.util.HashMap" scope="request"/>

<div id="outer-container" class="tab-container">
    <ul class='tabs-nav'>
        <c:forEach var="item" items="${reportPages}">
            <li class='tab'><a href="#${item.key}">${item.key}</a></li>
        </c:forEach>
    </ul>

    <c:forEach var="item" items="${reportPages}">
        <div id="${item.key}">
            <div class="panel-container" id="${item.key}-inner">
                <ul class="tabs-nav">
                    <li class="tab"><a href="#${item.key}-abi">Binary Compatibility</a></li>
                    <li class="tab"><a href="#${item.key}-src">Source Compatibility</a></li>
                </ul>

                <div id="${item.key}-abi">
                        ${item.value.key}
                </div>
                <div id="${item.key}-src">
                        ${item.value.value}
                </div>
            </div>
        </div>
    </c:forEach>
</div>

<script>
    jQuery(document).ready(function () {
        jQuery('#outer-container').easytabs({animate: false});
        <c:forEach var="item" items="${reportPages}">
        jQuery('#${item.key}-inner').easytabs({animate: false});
        </c:forEach>
    });
</script>

<style type="text/css">

.tabs-nav {
    margin: 0 1px;
    padding: 0;
    text-align: left;
}

.tab {
    display: inline-block;
    zoom: 1;
    *display: inline;
    background: #F2F3F5;
    border-bottom: none;
}

.tab a {
    line-height: 2em;
    display: block;
    padding: 0 10px;
    outline: none;
}

.tab a:hover {
    text-decoration: underline;
}

.tab a.active {
    color: black;
}

.tab a.active:hover {
    text-decoration: none;
}

.tab.active {
    background: #fff;
    position: relative;
    top: 1px;
    border: 1px solid #CECECE;
    border-bottom: none;
}

.tab-container .panel-container {
    background: #fff;
    border: 1px solid #CECECE;
    padding: 10px;
    margin: 0 1px;
}

    /* ABI report styles */
h2 {
    margin-top: 7px;
}

span.section {
    font-weight: bold;
    cursor: pointer;
    font-size: 16px;
    color: #003E69;
    white-space: nowrap;
    margin-left: 5px;
}

span.new_sign {
    font-weight: bold;
    margin-left: 26px;
    font-size: 16px;
    color: #003E69;
}

span.new_sign_lbl {
    margin-left: 28px;
    font-size: 14px;
    color: black;
}

span:hover.section {
    color: #336699;
}

span.section_affected {
    cursor: pointer;
    margin-left: 7px;
    padding-left: 15px;
    font-size: 14px;
    color: #cc3300;
}

span.section_info {
    cursor: pointer;
    margin-left: 7px;
    padding-left: 15px;
    font-size: 14px;
    color: black;
}

span.extendable {
    font-weight: 100;
    font-size: 16px;
}

span.h_name {
    color: #cc3300;
    font-size: 14px;
    font-weight: bold;
}

div.h_list {
    font-size: 15px;
    padding-left: 5px;
}

span.ns {
    color: #408080;
    font-size: 15px;
}

div.lib_list {
    font-size: 15px;
    padding-left: 5px;
}

span.lib_name {
    color: Green;
    font-size: 14px;
    font-weight: bold;
}

span.iname {
    font-weight: bold;
    font-size: 16px;
    color: #003E69;
    margin-left: 5px;
}

span.iname_b {
    font-weight: bold;
    font-size: 15px;
}

span.iname_a {
    color: #333333;
    font-weight: bold;
    font-size: 15px;
}

span.sym_p {
    font-weight: normal;
    white-space: normal;
}

span.sym_kind {
    color: Black;
    font-weight: normal;
}

div.affect {
    padding-left: 15px;
    padding-bottom: 4px;
    font-size: 14px;
    font-style: italic;
    line-height: 13px;
}

div.affected {
    padding-left: 30px;
    padding-top: 5px;
}

table.ptable {
    border-collapse: collapse;
    border: 1px outset black;
    line-height: 16px;
    margin-left: 15px;
    margin-top: 3px;
    margin-bottom: 3px;
    width: 900px;
}

table.ptable td {
    border: 1px solid #CECECE;
    padding: 3px;
}

table.vtable {
    border-collapse: collapse;
    border: 1px outset black;
    line-height: 16px;
    margin-left: 30px;
    margin-top: 10px;
    width: 100px;
}

table.vtable td {
    border: 1px solid #CECECE;
    white-space: nowrap;
    padding: 3px;
}

table.ptable th, table.vtable th {
    background-color: #F2F3F5;
    font-weight: bold;
    color: #333333;
    font-size: 13px;
    border: 1px solid #CECECE;
    text-align: center;
    vertical-align: top;
    white-space: nowrap;
    padding: 3px;
}

table.summary {
    border-collapse: collapse;
}

table.summary th {
    background-color: #F2F3F5;
    font-weight: 100;
    text-align: left;
    font-size: 15px;
    white-space: nowrap;
    border: 1px inset #CECECE;
    padding: 3px;
    width: 200px;
}

table.summary td {
    text-align: right;
    font-size: 16px;
    white-space: nowrap;
    border: 1px inset #CECECE;
    padding: 3px 5px 3px 10px;
}

table.code_view {
    cursor: text;
    margin-top: 7px;
    margin-left: 15px;
    font-family: Monaco, Consolas, 'DejaVu Sans Mono', 'Droid Sans Mono', Monospace;
    font-size: 14px;
    padding: 10px;
    border: 1px solid #e0e8e5;
    color: #444444;
    background-color: #eff3f2;
    overflow: auto;
}

table.code_view td {
    padding-left: 15px;
    text-align: left;
    white-space: nowrap;
}

span.mangled {
    padding-left: 15px;
    font-size: 14px;
    cursor: text;
    color: #444444;
}

span.sym_ver {
    color: #333333;
    white-space: nowrap;
}

span.color_p {
    font-style: italic;
    color: brown;
}

span.param {
    font-style: italic;
}

span.focus_p {
    font-style: italic;
    color: red;
}

span.ttype {
    font-weight: 100;
}

span.nowrap {
    white-space: nowrap;
}

span.value {
    white-space: nowrap;
    font-weight: bold;
}

td.passed {
    background-color: #CCFFCC;
}

td.warning {
    background-color: #F4F4AF;
}

td.failed {
    background-color: #FFCCCC;
}

td.new {
    background-color: #C6DEFF;
}

.tabset {
    float: left;
}

a.tab {
    border: 1px solid #AAA;
    float: left;
    margin: 0px 5px -1px 0px;
    padding: 3px 5px 3px 5px;
    position: relative;
    font-size: 14px;
    background-color: #DDD;
    text-decoration: none;
    color: Black;
}

a.disabled:hover {
    color: Black;
    background: #F2F3F5;
}

a.active:hover {
    color: Black;
    background: White;
}

a.active {
    border-bottom-color: White;
    background-color: White;
}

div.tab {
    border: 1px solid #AAA;
    padding: 0 7px 0 12px;
    width: 97%;
    clear: both;
}
</style>