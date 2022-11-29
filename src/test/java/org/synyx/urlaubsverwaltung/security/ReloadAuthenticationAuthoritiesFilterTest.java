package org.synyx.urlaubsverwaltung.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.synyx.urlaubsverwaltung.person.Role.INACTIVE;
import static org.synyx.urlaubsverwaltung.person.Role.OFFICE;
import static org.synyx.urlaubsverwaltung.person.Role.USER;

@ExtendWith(MockitoExtension.class)
class ReloadAuthenticationAuthoritiesFilterTest {

    private ReloadAuthenticationAuthoritiesFilter sut;

    @Mock
    private PersonService personService;
    @Mock
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        sut = new ReloadAuthenticationAuthoritiesFilter(personService, sessionService);
    }

    @Test
    void ensuresFilterSetsOAuth2AuthenticationWithNewAuthorities() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.getSession().setAttribute("reloadAuthorities", true);

        final Person signedInUser = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        SecurityContextHolder.getContext().setAuthentication(prepareOAuth2Authentication());

        sut.doFilterInternal(request, response, filterChain);

        final List<String> updatedAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(toList());
        assertThat(updatedAuthorities).containsExactly("USER", "OFFICE");

        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
    }

    @Test
    void ensuresFilterSetsUsernameAndPasswordAuthenticationWithNewAuthorities() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.getSession().setAttribute("reloadAuthorities", true);

        final Person signedInUser = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        SecurityContextHolder.getContext().setAuthentication(prepareUsernameAndPasswordAuthentication());

        sut.doFilterInternal(request, response, filterChain);

        final List<String> updatedAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(toList());
        assertThat(updatedAuthorities).containsExactly("USER", "OFFICE");

        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
    }

    @Test
    void ensuresToNotUpdateAuthoritiesOnUnknownAuthenticationType() throws ServletException, IOException {

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain filterChain = new MockFilterChain();

        request.getSession().setAttribute("reloadAuthorities", true);

        final Person signedInUser = new Person("marlene", "Muster", "Marlene", "muster@example.org");
        signedInUser.setPermissions(List.of(USER, OFFICE));
        when(personService.getSignedInUser()).thenReturn(signedInUser);

        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("principle", "credentials", "INACTIVE"));

        sut.doFilterInternal(request, response, filterChain);

        final List<String> updatedAuthorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(toList());
        assertThat(updatedAuthorities).containsExactly("INACTIVE");

        verify(sessionService).unmarkSessionToReloadAuthorities(request.getSession().getId());
    }

    @Test
    void ensuresFilterSetsAuthenticationWithNewAuthoritiesButSessionIsNullDoNothing() {

        final MockHttpServletRequest request = mock(MockHttpServletRequest.class);
        when(request.getSession()).thenReturn(null);

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void ensuresFilterSetsNoNewAuthenticationIfReloadIsNotDefined() {

        final MockHttpServletRequest request = new MockHttpServletRequest();

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void ensuresFilterSetsNoNewAuthenticationIfReloadIsFalse() {

        final MockHttpServletRequest request = new MockHttpServletRequest();

        request.getSession().setAttribute("reloadAuthorities", false);

        final boolean shouldNotFilter = sut.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    private OAuth2AuthenticationToken prepareOAuth2Authentication() {
        final OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        final OidcUser oidcUser = mock(OidcUser.class);
        when(authentication.getPrincipal()).thenReturn(oidcUser);
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("authorizedClientRegistrationId");
        return authentication;
    }

    private UsernamePasswordAuthenticationToken prepareUsernameAndPasswordAuthentication() {
        final UsernamePasswordAuthenticationToken authentication = mock(UsernamePasswordAuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn("username");
        return authentication;
    }
}
