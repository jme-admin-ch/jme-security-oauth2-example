package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import static ch.admin.bit.jeap.jme.security.oauth.client.SanitizeUtil.sanitize;

/**
 * This endpoint makes the OAuth2 protected endpoints of the jme-security-oauth2-client-resource's 'thing' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints acting as an OAuth2 client, i.e. equipping
 * the requests with an appropriate authorization token.
 */
@RestController
@RequestMapping
@Slf4j
public class ThingController {

    private final RestClient restClient;

    public ThingController(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory, TargetConfigProps targetConfigProps) {
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all
        // RestClient instances built with this builder will add an OAuth2 access token
        // to the exchanges based on the provided Spring client registration as defined in the application*.yml.
        this.restClient = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId("jme-security-oauth2-client-service")
                         .baseUrl(targetConfigProps.getResource()).build();
    }

    @GetMapping("/api/things")
    public String listThings() {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor).
        String response = restClient.get().
                uri("/api/things").
                retrieve().
                body(String.class);
        return String.format("Got things: %s", response);
    }

    @GetMapping("/api/partners/{partnerId}/things")
    public String listThingsForPartner(@PathVariable("partnerId") String partnerId) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor).
        String response = restClient.get().
                uri("/api/partners/{partnerId}/things", partnerId).
                retrieve().
                body(String.class);
       return String.format("Got things for partner with id %s: %s", sanitize(partnerId), response);
    }

    @GetMapping("/api/things/{id}")
    public String getThingById(@PathVariable("id") String id) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor)
        String response = restClient.get().
                uri( "/api/things/{id}", id).
                retrieve().
                body(String.class);
        return String.format("Got thing with id '%s': %s", sanitize(id), response);
    }

}
