package org.synyx.urlaubsverwaltung.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.security.oidc.OidcSecurityProperties;

@Configuration
@ConditionalOnProperty(value = "uv.security.auth", havingValue = "oidc")
@Order(2)
public class Oauth2RestApiSecurityConfig extends WebSecurityConfigurerAdapter {

    private final String issuerUri;
    private final PersonService personService;

    public Oauth2RestApiSecurityConfig(OidcSecurityProperties properties, PersonService personService) {
        this.issuerUri = properties.getIssuerUri();
        this.personService = personService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
            .jwtAuthenticationConverter(jwtAuthenticationConverter())
            .decoder(jwtDecoder())
        ));

        http.oauth2Login();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new JwtToPersonGrantedAuthoritiesConverter(personService));
        return jwtAuthenticationConverter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
}
