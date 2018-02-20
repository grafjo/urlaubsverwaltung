$(function () {

    var currentYear = new Date().getFullYear();

    var $dropdown = $('#year-selection').find('.dropdown-menu');

    $dropdown.append('<li><a href="${hrefPrefix}' + (currentYear + 1) + '">' + (currentYear + 1) + '</a></li>');
    $dropdown.append('<li><a href="${hrefPrefix}' + currentYear + '">' + currentYear + '</a></li>');

    for (var i = 1; i < 10; i++) {
        $dropdown.append('<li><a href="${hrefPrefix}' + (currentYear - i) + '">' + (currentYear - i) + '</a></li>');
    }

});

$(function() {

    var datepickerLocale = window.calendarInit.datepickerLocale;
    var personId = window.calendarInit.personId;
    var webPrefix = window.calendarInit.webPrefix;
    var apiPrefix = window.calendarInit.apiPrefix;

    // calendar is initialised when moment.js AND moment.language.js are loaded
    function initCalendar() {
        var year = window.app.custom.getUrlParam("year");
        var date = moment();

        if (year.length > 0 && year != date.year()) {
            date.year(year).month(0).date(1);
        }

        var holidayService = Urlaubsverwaltung.HolidayService.create(webPrefix, apiPrefix, + personId);

        // NOTE: All moments are mutable!
        var startDateToCalculate = date.clone();
        var endDateToCalculate = date.clone();
        var shownNumberOfMonths = 10;
        var startDate = startDateToCalculate.subtract(shownNumberOfMonths/2, 'months');
        var endDate = endDateToCalculate.add(shownNumberOfMonths/2, 'months');

        var yearOfStartDate = startDate.year();
        var yearOfEndDate = endDate.year();

        $.when(
            holidayService.fetchPublic   ( yearOfStartDate ),
            holidayService.fetchPersonal ( yearOfStartDate ),
            holidayService.fetchSickDays ( yearOfStartDate ),

            holidayService.fetchPublic   ( yearOfEndDate ),
            holidayService.fetchPersonal ( yearOfEndDate ),
            holidayService.fetchSickDays ( yearOfEndDate )
        ).always(function() {
            Urlaubsverwaltung.Calendar.init(holidayService, date);
        });
    }

    initCalendar();

    var resizeTimer = null;

    $(window).on('resize', function () {

        if (resizeTimer !== null) {
            clearTimeout(resizeTimer);
        }

        resizeTimer = setTimeout(function () {
            Urlaubsverwaltung.Calendar.reRender();
            resizeTimer = null;
        }, 30)

    });

});
