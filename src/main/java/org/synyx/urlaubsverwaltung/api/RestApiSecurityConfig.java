package org.synyx.urlaubsverwaltung.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.synyx.urlaubsverwaltung.security.SecurityConfigurationProperties;

import static org.springframework.security.config.http.SessionCreationPolicy.NEVER;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@Order(1)
public class RestApiSecurityConfig {

    private final boolean isOauth2Enabled;

    public RestApiSecurityConfig(SecurityConfigurationProperties properties) {
        isOauth2Enabled = "oidc".equalsIgnoreCase(properties.getAuth().name());
    }

    @Bean
    public SecurityFilterChain restApiSecurityFilterChain(HttpSecurity http) throws Exception {


        http.securityMatcher(antMatcher("/api/**"))
            .authorizeHttpRequests(
                authorizeHttpRequests -> authorizeHttpRequests.anyRequest().authenticated()
            ).sessionManagement(
                sessionManagement -> sessionManagement.sessionCreationPolicy(NEVER)
            );

        if (isOauth2Enabled) {
            http.securityMatcher("/api/**").oauth2Login();
        } else {
            http.securityMatcher("/api/**").httpBasic();
        }

        return http.build();
    }
}
