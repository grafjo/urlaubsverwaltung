package org.synyx.urlaubsverwaltung.security.oidc;

import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class GroupClaimRoleAccessDecisionVoter implements AccessDecisionVoter<Object> {

    // TODO make this constants configurable from application.properties
    private static final String REQUIRED_GROUP = "urlaubsverwaltung-user";


    @Override
    public boolean supports(ConfigAttribute attribute) {
        return (attribute != null && "authenticated".equals(attribute.toString()));
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

        if (authentication == null) {
            return ACCESS_DENIED;
        }

        int result = ACCESS_ABSTAIN;

        for (ConfigAttribute attribute : attributes) {
            if (supports(attribute)) {
                result = ACCESS_DENIED;

                for (GrantedAuthority authority : authentication.getAuthorities()) {
                    if (REQUIRED_GROUP.equals(authority.getAuthority())) {
                        return ACCESS_GRANTED;
                    }
                }
            }
        }

        return result;
    }
}
