Hallo ${recipient.niceName},

Die folgenden Urlaubsanträge warten auf Bearbeitung:

<#list applicationList as application>
Antrag von ${application.person.niceName} vom ${application.applicationDate.format("dd.MM.yyyy")}: ${applicationUrl}web/application/${application.id?c}
</#list>
