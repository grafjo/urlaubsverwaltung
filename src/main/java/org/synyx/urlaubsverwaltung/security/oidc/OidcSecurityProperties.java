package org.synyx.urlaubsverwaltung.security.oidc;

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Florian Krupicka - krupicka@synyx.de
 */
@Validated
@ConfigurationProperties("uv.security.oidc")
public class OidcSecurityProperties {

    /**
     * The OpenId Connect issuer URI (e.g. your OIDC server).
     */
    @URL
    private String issuerUri;

    /**
     * OIDC client identifier to authenticate the UV application against your OIDC authentication server.
     */
    @NotEmpty
    private String clientId;

    /**
     * OIDC client secret to authenticate the UV application against your OIDC authentication server.
     */
    @NotEmpty
    private String clientSecret;

    /**
     * The logout URI where the logout is defined for the used provider.
     *
     * <p>e.g. for <i>keycloak</i> this would be
     * <pre>'https://$provider/auth/realms/$realm/protocol/openid-connect/logout'</pre>
     * </p>
     *
     * @deprecated
     */
    @NotEmpty
    @Deprecated(forRemoval = true, since = "4.49.0")
    private String logoutUri;

    /**
     * OIDC client scopes to receive family name, given name and email in the OIDC access token.
     */
    @NotEmpty
    private List<String> scopes = List.of("openid", "profile", "email");

    /**
     * OIDC post logout redirect uri.
     * <p>
     * Redirects the user to the given url after logout.
     * Default is the base url of the request.
     */
    @NotEmpty
    private String postLogoutRedirectUri = "{baseUrl}";

    @NotNull
    private GroupClaim groupClaim;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getLogoutUri() {
        return logoutUri;
    }

    public void setLogoutUri(String logoutUri) {
        this.logoutUri = logoutUri;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public String getPostLogoutRedirectUri() {
        return postLogoutRedirectUri;
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public GroupClaim getGroupClaim() {
        return groupClaim;
    }

    public void setGroupClaim(GroupClaim groupClaim) {
        this.groupClaim = groupClaim;
    }

    public static class GroupClaim {

        private boolean enabled = false;

        @NotEmpty
        private String claimName = "groups";

        @NotEmpty
        private String permittedGroup = "urlaubsverwaltung-user";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getClaimName() {
            return claimName;
        }

        public void setClaimName(String claimName) {
            this.claimName = claimName;
        }

        public String getPermittedGroup() {
            return permittedGroup;
        }

        public void setPermittedGroup(String permittedGroup) {
            this.permittedGroup = permittedGroup;
        }
    }
}
