package org.synyx.urlaubsverwaltung.restapi;


import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.util.StringUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.synyx.urlaubsverwaltung.core.application.domain.DayLength;
import org.synyx.urlaubsverwaltung.core.calendar.OwnCalendarService;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * @author  Aljona Murygina - murygina@synyx.de
 */
@Api(value = "Work Days", description = "Get information about work day in a certain period")
@Controller("restApiWorkDayController")
public class WorkDayController {

    private static final String ROOT_URL = "/workdays";

    @Autowired
    private PersonService personService;

    @Autowired
    private OwnCalendarService ownCalendarService;

    /**
     * Calculate number of work days for the given period and person.
     *
     * @param  from  start date as String (e.g. 2013-3-21)
     * @param  to  end date as String (e.g. 2013-3-21)
     * @param  length  day length as String (FULL, MORNING or NOON)
     * @param  personId  id of the person to number of work days for
     *
     * @return  number of days as String for the given parameters or "N/A" if parameters are not valid in any way
     */
    @ApiOperation(
        value = "Calculate the work days for a certain period and person",
        notes =
            "Calculate the work days for a certain period and person. The calculation depends on the working time of the person."
    )
    @RequestMapping(value = ROOT_URL, method = RequestMethod.GET)
    @ResponseBody
    public String workDays(
        @ApiParam(value = "Start date with pattern yyyy-MM-dd", defaultValue = "2015-01-01")
        @RequestParam("from")
        String from,
        @ApiParam(value = "End date with pattern yyyy-MM-dd", defaultValue = "2015-01-08")
        @RequestParam("to")
        String to,
        @ApiParam(value = "Day Length", defaultValue = "FULL")
        @RequestParam("length")
        String length,
        @ApiParam(value = "ID of the person")
        @RequestParam("person")
        Integer personId) {

        if (StringUtils.hasText(from) && StringUtils.hasText(to) && StringUtils.hasText(length)) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(RestApiDateFormat.PATTERN);
            DateMidnight startDate = DateMidnight.parse(from, fmt);
            DateMidnight endDate = DateMidnight.parse(to, fmt);

            if (startDate.isBefore(endDate) || startDate.isEqual(endDate)) {
                DayLength howLong = DayLength.valueOf(length);
                Optional<Person> person = personService.getPersonByID(personId);

                if (!person.isPresent()) {
                    return "N/A";
                }

                BigDecimal days = ownCalendarService.getWorkDays(howLong, startDate, endDate, person.get());

                return days.toString();
            }
        }

        return "N/A";
    }
}
