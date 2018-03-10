<script>
    window.calendarInit = {};
    window.calendarInit.hrefPrefix = "<c:out value='${URL_PREFIX}/staff/${person.id}/overview?year='/>";
    window.calendarInit.datepickerLocale = "${pageContext.response.locale.language}";
    window.calendarInit.personId = "<c:out value='${person.id}' />";
    window.calendarInit.webPrefix = "<spring:url value='/web' />";
    window.calendarInit.apiPrefix = "<spring:url value='/api' />";
</script>