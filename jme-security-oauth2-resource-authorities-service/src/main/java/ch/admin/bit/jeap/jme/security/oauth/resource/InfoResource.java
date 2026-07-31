package ch.admin.bit.jeap.jme.security.oauth.resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class gives an example for a different REST resource that requires a different authority than the Thing
 * resource. It is protected by the same OAuth2 configuration as the {@link ThingResource}, though.
 */
@RestController
@RequestMapping("/api/info/**")
public class InfoResource {

    @GetMapping
    @PreAuthorize("hasAuthority('info:read')")
    public String getInfo() {
        return "Some info";
    }

}

