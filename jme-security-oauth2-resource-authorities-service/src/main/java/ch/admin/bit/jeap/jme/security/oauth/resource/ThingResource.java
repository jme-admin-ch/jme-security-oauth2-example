package ch.admin.bit.jeap.jme.security.oauth.resource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

/**
 * This class gives an example for an OAuth2 protected resource 'thing' that requires certain authorities for access.
 * The thing resource manages data of things. To access a thing's detail data
 * a user is required to have the authority 'things:detail', an overview of all things can be aquired having the
 * authority 'things:read'.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class ThingResource {

    private Set<Thing> things = Set.of(
            new Thing("1", "Thing1"),
            new Thing("2", "Thing2"),
            new Thing("3", "Thing3"),
            new Thing("8", "Thing8"),
            new Thing("9", "Thing9"));

    @GetMapping("/api/things")
    @PreAuthorize("hasAuthority('things:read')")
    public Collection<Thing> listThings() {
        return listAll();
    }

    /**
     * NB: the {@link ExampleAuthoritiesResolver} only grants the authority `things:detail` for users with role 'admin'.
     */
    @GetMapping("/api/things/{id}")
    @PreAuthorize("hasAuthority('things:detail')")
    @PostAuthorize("hasPermission(returnObject, 'detail')")
    public Thing getThingById(@PathVariable("id") String id) {
        return findThingById(id).orElseThrow(supplyThingNotFoundStatusException(id));
    }

    private Supplier<ResponseStatusException> supplyThingNotFoundStatusException(final String thingId) {
        return () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thing with id '" + thingId + "' not found");
    }

    private Collection<Thing> listAll() {
        return things;
    }

    private Optional<Thing> findThingById(String id) {
        return things.stream().filter(thing -> thing.getId().equals(id)).findFirst();
    }
}
