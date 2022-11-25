<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@attribute name="year" type="java.lang.String" required="true" %>
<%@attribute name="hrefPrefix" type="java.lang.String" required="true" %>

<jsp:useBean id="date" class="java.util.Date" />

<div id="year-selection" class="tw-leading-6 dropdown tw-inline-block">
    <a
        id="year-selector-dropdown-link"
        href="#"
        data-toggle="dropdown"
        aria-haspopup="true"
        role="button"
        aria-expanded="false"
        class="tw-text-current"
    >
        <c:out value="${year}" /><span class="tw-ml-0.5 dropdown-caret tw-opacity-70"></span>
    </a>
    <ul class="dropdown-menu" role="menu" aria-labelledby="year-selector-dropdown-link">
        <c:forEach begin="0" end="10" varStatus="loop">
            <c:set var="y" value="${date.year + 1900 + 2 - loop.count}" />
            <li><a href="${hrefPrefix.concat(y)}"><c:out value="${y}" /></a></li>
        </c:forEach>
    </ul>
</div>
