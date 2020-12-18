package org.synyx.urlaubsverwaltung.api;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class NonOidcSecurityAuthCondition implements Condition {

    private static final String UV_SECURITY_AUTH = "uv.security.auth";
    private static final String OIDC = "oidc";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        String value = conditionContext.getEnvironment().getProperty(UV_SECURITY_AUTH);
        return !OIDC.equalsIgnoreCase(value);
    }
}
