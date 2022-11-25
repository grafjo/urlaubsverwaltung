<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="account" type="org.synyx.urlaubsverwaltung.account.Account" required="true" %>
<%@attribute name="className" type="java.lang.String" required="false" %>
<%@attribute name="noPadding" type="java.lang.Boolean" required="false" %>

<c:set var="paddingCssClass" value="${noPadding ? 'tw-p-0' : 'tw-p-5'}" />

<uv:account-entitlement-box__ account="${account}" className="${paddingCssClass} ${className}" />
