<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@attribute name="number" type="java.math.BigDecimal" required="true" %>

<fmt:formatNumber maxFractionDigits="2" value="${number}" />
