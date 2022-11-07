package org.synyx.urlaubsverwaltung.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.synyx.urlaubsverwaltung.person.PersonService;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private static final String OFFICE = "OFFICE";
    private static final String BOSS = "BOSS";
    private static final String USER = "USER";
    private static final String ADMIN = "ADMIN";

    private final PersonService personService;
    private final SessionService sessionService;
    private final boolean isOauth2Enabled;

    private OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler;

    public WebSecurityConfig(SecurityConfigurationProperties properties, PersonService personService, SessionService sessionService) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
        this.personService = personService;
        this.sessionService = sessionService;
    }

    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests((authorizeHttpRequests) ->
                authorizeHttpRequests
                    .requestMatchers("/favicons/**").permitAll()
                    .requestMatchers("/browserconfig.xml").permitAll()
                    .requestMatchers("/manifest.json").permitAll()
                    .requestMatchers("/css/**").permitAll()
                    .requestMatchers("/fonts/**").permitAll()
                    .requestMatchers("/images/**").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    // WEB
                    .requestMatchers("/login*").permitAll()
                    .requestMatchers(GET, "/web/company/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/departments/*/persons/*/calendar").permitAll()
                    .requestMatchers(GET, "/web/persons/*/calendar").permitAll()
                    .requestMatchers("/web/absences/**").hasAuthority(USER)
                    .requestMatchers("/web/application/**").hasAuthority(USER)
                    .requestMatchers("/web/department/**").hasAnyAuthority(BOSS, OFFICE)
                    .requestMatchers("/web/google-api-handshake/**").hasAuthority(OFFICE)
                    .requestMatchers("/web/overview").hasAuthority(USER)
                    .requestMatchers("/web/overtime/**").hasAuthority(USER)
                    .requestMatchers("/web/person/**").hasAuthority(USER)
                    .requestMatchers("/web/sicknote/**").hasAuthority(USER)
                    .requestMatchers("/web/settings/**").hasAuthority(OFFICE)
                    .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                    .requestMatchers(EndpointRequest.to(PrometheusScrapeEndpoint.class)).permitAll()
                    // TODO muss konfigurierbar werden!
                    .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(ADMIN)
                    .anyRequest()
                    .authenticated()
            );


        if (isOauth2Enabled) {
            http.oauth2Login().and()
                .logout()
                .logoutSuccessHandler(oidcClientInitiatedLogoutSuccessHandler);
        } else {
            http.formLogin()
                .loginPage("/login").permitAll()
                .defaultSuccessUrl("/web/overview")
                .failureUrl("/login?login_error=1")
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login");
        }

        http
            .addFilterAfter(new ReloadAuthenticationAuthoritiesFilter(personService, sessionService), BasicAuthenticationFilter.class);

        return http.build();
    }

    @Autowired(required = false)
    public void setOidcClientInitiatedLogoutSuccessHandler(OidcClientInitiatedLogoutSuccessHandler oidcClientInitiatedLogoutSuccessHandler) {
        this.oidcClientInitiatedLogoutSuccessHandler = oidcClientInitiatedLogoutSuccessHandler;
    }
}
