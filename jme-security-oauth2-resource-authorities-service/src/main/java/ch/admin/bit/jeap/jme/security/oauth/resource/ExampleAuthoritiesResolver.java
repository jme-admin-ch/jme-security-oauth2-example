package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.token.AuthoritiesResolver;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The `{@link ch.admin.bit.jeap.security.resource.token.DefaultAuthoritiesResolver}` packaged by the jeap library is
 * configured as "conditional on missing bean". This means that any of your classes implementing
 * {@link AuthoritiesResolver} will override the default behaviour.
 */
@Component
public class ExampleAuthoritiesResolver implements AuthoritiesResolver {
    @Override
    public Collection<GrantedAuthority> deriveAuthoritiesFromRoles(Set<String> userRoles, Map<String, Set<String>> businessPartnerRoles) {
        List<String> grantedAuthorities = new ArrayList<>(List.of("things:read", "info:read"));
        if (userRoles.contains("admin")) {
            grantedAuthorities.add("things:detail");
        }
        return grantedAuthorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }
}
