package org.synyx.urlaubsverwaltung.workingtime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Component
@ConfigurationProperties("uv.workingtime")
@Validated
public class WorkingTimeProperties {

    /**
     * Define the default working days that will be configured for
     * every newly created person.
     * <p>
     * Default values: Monday, Tuesday, Wednesday, Thursday, Friday
     */
    @NotNull
    @Deprecated
    private List<@Min(-1) @Max(7) Integer> defaultWorkingDays = List.of(1, 2, 3, 4, 5);

    @Deprecated
    public List<Integer> getDefaultWorkingDays() {
        return defaultWorkingDays;
    }

    @Deprecated
    public void setDefaultWorkingDays(List<Integer> defaultWorkingDays) {
        this.defaultWorkingDays = defaultWorkingDays;
    }

    @Deprecated
    public boolean isDefaultWorkingDaysDeactivated() {
        return defaultWorkingDays.get(0) == -1;
    }
}
