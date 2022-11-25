<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="uv" tagdir="/WEB-INF/tags" %>

<%@attribute name="person" type="org.synyx.urlaubsverwaltung.person.Person" required="true" %>
<%@attribute name="departmentsOfPerson" type="java.util.List<org.synyx.urlaubsverwaltung.department.Department>" required="false" %>
<%@attribute name="cssClass" type="java.lang.String" required="false" %>
<%@attribute name="nameIsNoLink" type="java.lang.Boolean" required="false" %>

<uv:person-box__ person="${person}" departmentsOfPerson="${departmentsOfPerson}" nameIsNoLink="${nameIsNoLink}" cssClass="tw-p-0 ${cssClass}" />
