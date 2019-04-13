Hallo Office,

es wurden Überstunden erfasst: ${applicationUrl}web/overtime/${overtime.id?c}

Mitarbeiter: ${overtime.person.niceName}

Datum: ${overtime.startDate.format("dd.MM.yyyy")} - ${overtime.endDate.format("dd.MM.yyyy")}
Anzahl der Stunden: ${overtime.hours}

<#if (comment.text)?has_content>
Kommentar von ${comment.person.niceName} zum Überstundeneintrag: ${comment.text}
</#if>
