<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="joda" uri="http://www.joda.org/joda/time/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<legend>

    <spring:message code="overview.title"/>

    <c:choose>
        <c:when test="${!empty param.year}">
            <c:set var="displayYear" value="${param.year}"/>
        </c:when>
        <c:otherwise>
            <c:set var="displayYear" value="${year}"/>
        </c:otherwise>
    </c:choose>

    <div id="year-selection" class="legend-dropdown dropdown">
        <a id="dropdownLabel" href="#" data-toggle="dropdown"
           aria-haspopup="true" role="button" aria-expanded="false">
            <c:out value="${displayYear}" /><span class="caret"></span>
        </a>

        <ul class="dropdown-menu" role="menu" aria-labelledby="dropdownLabel"></ul>
    </div>

    <uv:print/>

    <a href="${URL_PREFIX}/staff/${person.id}" class="fa-action pull-right" style="margin-top: 1px"
       data-title="<spring:message code="action.details"/>">
        <i class="fa fa-list-alt"></i>
    </a>

</legend>
