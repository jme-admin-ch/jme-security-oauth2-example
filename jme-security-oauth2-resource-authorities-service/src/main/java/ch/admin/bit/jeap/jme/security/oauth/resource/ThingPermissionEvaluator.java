package ch.admin.bit.jeap.jme.security.oauth.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;

/**
 * An example for a custom permission evaluator. Permission evaluators may be used for special authorization needs.
 */
@Slf4j
public class ThingPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permissionObject) {
        if (targetDomainObject instanceof Thing && permissionObject instanceof String permission) {
            return authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("things:" + permission));
        } else {
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetIdSerializable, String targetType, Object permissionObject) {
        // unsupported permission check type in this example
        return false;
    }

}