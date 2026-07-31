package ch.admin.bit.jeap.oauth.mock.server.token;

import ch.admin.bit.jeap.oauth.mock.server.config.ClientData;
import ch.admin.bit.jeap.oauth.mock.server.config.OAuthMockData.UserData;
import ch.admin.bit.jeap.oauth.mock.server.login.CustomLoginDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.oidc.OidcClientMetadataClaimNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

/**
 * Example access token customizer for simulating access tokens as issued by eIAM.
 * This OAuth2TokenCustomizer implementation replaces the default implementation provided by the OAuth mock server.
 */
@Component
class EiamJwtAccessTokenCustomizer extends AbstractJwtTokenCustomizer {

    @Override
    protected void customizeAccessToken(JwtEncodingContext context, Map<String, Object> claims) {
        String clientId = getClientIdFromSecurityContext();
        setClaims(clientId, claims, context.getPrincipal());
    }

    @Override
    protected void customizeIdToken(JwtEncodingContext context, Map<String, Object> claims) {
        String clientId = getClientIdFromSecurityContext();
        setClaims(clientId, claims, context.getPrincipal());
        updateClaimsForIdToken(clientId, claims);
    }

    private void setClaims(String clientId, Map<String, Object> claims, Authentication userAuthentication) {
        RegisteredClient clientData = requireClient(clientId);
        UserData userData = getUserDataIfInUserContext(userAuthentication);

        setSubject(clientData, userData, claims);
        setUserClaims(userData, claims);
        setRoles(userData, clientData, claims);
        setAudience(clientData, claims);
    }

    private void setSubject(RegisteredClient client, UserData userData, Map<String, Object> additionalInfo) {
        String subject = subjectOrUuid(client, userData);
        additionalInfo.put(StandardClaimNames.SUB, subject);
    }

    private void setAudience(RegisteredClient client, Map<String, Object> additionalInfo) {
        List<String> audience = ClientData.getAudience(client);
        if (audience != null) {
            additionalInfo.put(IdTokenClaimNames.AUD, audience);
        } else {
            additionalInfo.remove(IdTokenClaimNames.AUD);
        }
    }

    private void setRoles(UserData user, RegisteredClient client, Map<String, Object> additionalInfo) {
        // Use the roles defined on the user if provided, else use the roles defined on the client.
        // eIAM only provides simple roles (userroles), i.e. no business partner specific roles (bproles)
        List<String> roles = (user != null ? user.getUserroles() : ClientData.getUserRolesForClient(client));
        if (roles != null) {
            additionalInfo.put(EiamClaims.ROLE.claim(), roles);
        }
    }

    private void setUserClaims(UserData user, Map<String, Object> additionalInfo) {
        if (user != null) {
            additionalInfo.put(StandardClaimNames.NAME, user.getName());
            additionalInfo.put(StandardClaimNames.GIVEN_NAME, user.getGivenName());
            additionalInfo.put(StandardClaimNames.FAMILY_NAME, user.getFamilyName());
            additionalInfo.put(EiamClaims.LANGUAGE.claim(), user.getLocale().toLowerCase());
            additionalInfo.put(StandardClaimNames.PREFERRED_USERNAME, user.getPreferredUsername());
            additionalInfo.put(EiamClaims.USER_EXT_ID.claim(), user.getExtId());
        }
    }

    private void updateClaimsForIdToken(String clientId, Map<String, Object> claims) {
        claims.remove(OidcClientMetadataClaimNames.SCOPE);
        // Audience must be set to the clientID for the ID token
        claims.put(IdTokenClaimNames.AUD, clientId);
    }

    private static String subjectOrUuid(RegisteredClient client, UserData userData) {
        if (userData != null && (hasText(userData.getSubject()) || hasText(userData.getPreferredUsername()))) {
            return hasText(userData.getPreferredUsername()) ? userData.getPreferredUsername() : userData.getSubject();
        } else {
            String subject = ClientData.getSubject(client);
            if (hasText(subject)) {
                return subject;
            }
        }
        return UUID.randomUUID().toString();
    }

    private UserData getUserDataIfInUserContext(Authentication userAuthentication) {
        if (userAuthentication == null || !(userAuthentication.getPrincipal() instanceof User user)) {
            return null;
        }

        CustomLoginDetails customLoginDetails = (CustomLoginDetails) userAuthentication.getDetails();
        UserData userDataDefaultsFromConfig = requireUser(user.getUsername());
        return customLoginDetails.toUserDataWithDefaults(userDataDefaultsFromConfig);
    }

    private enum EiamClaims {
        ROLE("role"),
        USER_EXT_ID("userExtId"),
        LANGUAGE("language");

        private final String claim;

        EiamClaims(String claim) {
            this.claim = claim;
        }

        public String claim() {
            return claim;
        }
    }

}
