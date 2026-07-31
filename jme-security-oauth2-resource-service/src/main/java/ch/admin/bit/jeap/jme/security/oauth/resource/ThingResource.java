package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

/**
 * This class gives an example for an OAuth2 protected resource 'thing' that requires certain roles for access.
 * The thing resource manages data of things that belong to business partners. To access a thing's data a user is
 * required to have the role 'thing_read' for the specific business partner to which the thing to be accessed belongs.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ThingResource {

    private final ServletSimpleAuthorization jeapAuthorization;

    private Set<Thing> things = Set.of(
            new Thing("1", "11111", "Thing1"),
            new Thing("2", "11111", "Thing2"),
            new Thing("3", "22222", "Thing3"),
            new Thing("8", "88888", "Thing8"),
            new Thing("9", "99999", "Thing9"));

    @GetMapping("/api/things")
    @PreAuthorize("hasRole('thing_read')")
    public Collection<Thing> listThings() {
        // Does the token grant read access on the things of all partners?
        if (jeapAuthorization.hasRoleForAllPartners("thing_read")) {
            // Fetch all things.
            return listAll();
        } else {
            // Determine the partners the token grants read access on things...
            Collection<String> partners = jeapAuthorization.getPartnersForRole("thing_read");
            // ...then only provide the things belonging to those partners.
            return listForPartners(partners);
        }
    }

    @GetMapping("/api/partners/{partnerId}/things")
    @PreAuthorize("hasRoleForPartner('thing_read', #partnerId)")
    public Collection<Thing> listThingsForBusinessPartner(@PathVariable("partnerId") String partnerId) {
        return listForPartners(singleton(partnerId));
    }

    /**
     * We can't do a detailed authorization check on the thing entering the method because we do not yet know the
     * partner to which the thing belongs. However, we can do this check when leaving the method, because the return
     * object contains the partner id. This web endpoint will not return the thing if the token does not contain the role
     * 'things_read' for the partner to which the thing belongs. If the return object would not contain the partner id,
     * the detailed authorization check would have to be done programmatically using the appropriate
     * ServletSimpleAuthorization bean method. See {@link #getThingById2(String)} for such an example.
     */
    @GetMapping("/api/things/{id:[0-4][0-9]*}")
    @PreAuthorize("hasRole('thing_read')")
    @PostAuthorize("hasRoleForPartner('thing_read', returnObject.getPartnerId())")
    public Thing getThingById1(@PathVariable("id") String id) {
        return findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
    }

    /**
     * Same as {@link #getThingById1(String)} but replacing the declarative @PostAuthorize() check with a programmatic check.
     * See {@link #getThingById1(String)} for explanation.
     */
    @GetMapping("/api/things/{id:[5-9][0-9]*}")
    @PreAuthorize("hasRole('thing_read')")
    public Thing getThingById2(@PathVariable("id") String id) {
        Thing thing = findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
        if (jeapAuthorization.hasRoleForPartner("thing_read", thing.getPartnerId())) {
            return thing;
        } else {
            throw new AccessDeniedException("Access to thing with id '" + id + "' denied.");
        }
    }

    private Supplier<ResponseStatusException> supplyThingNotFoundStatusException(final String thingId) {
        return () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thing with id '" + thingId + "' not found");
    }

    private Collection<Thing> listAll() {
        return things;
    }

    private Collection<Thing> listForPartners(Collection<String> partners) {
        return things.stream().filter(thing -> partners.contains(thing.getPartnerId())).collect(Collectors.toSet());
    }

    private Optional<Thing> findThingById(String id) {
        return things.stream().filter(thing -> thing.getId().equals(id)).findFirst();
    }
}
