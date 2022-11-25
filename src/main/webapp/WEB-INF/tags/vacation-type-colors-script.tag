<%@taglib prefix="c" uri="jakarta.tags.core" %>

<script>
    window.uv.vacationTypes = {};
    window.uv.vacationTypes.colors = {};<c:forEach items="${vacationTypeColors}" var="vacationTypeColor">
    window.uv.vacationTypes.colors[${vacationTypeColor.id}] = "${vacationTypeColor.color}";</c:forEach>
</script>
