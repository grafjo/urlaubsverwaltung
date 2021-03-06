Hallo ${application.person.niceName},

dein Antrag zum Stornieren deines bereits genehmigten Antrags vom
${application.startDate.format("dd.MM.yyyy")} bis ${application.endDate.format("dd.MM.yyyy")} wurde eingereicht.

<#if (comment.text)?has_content>
Kommentar zur Stornierung von ${comment.person.niceName} zum Antrag: ${comment.text}

</#if>
Es handelt sich um folgenden Urlaubsantrag: ${baseLinkURL}web/application/${application.id?c}

Überblick deiner offenen Stornierungsanträge findest du unter ${baseLinkURL}web/application#cancellation-requests
