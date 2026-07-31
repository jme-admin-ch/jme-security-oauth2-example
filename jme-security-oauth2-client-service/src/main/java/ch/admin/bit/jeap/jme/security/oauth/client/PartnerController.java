package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import static ch.admin.bit.jeap.jme.security.oauth.client.SanitizeUtil.sanitize;

/**
 * This endpoint makes the OAuth2 protected endpoints of the jme-security-oauth2-client-resource's 'partner' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints acting as an OAuth2 client, i.e. equipping
 * the requests with an appropriate authorization token.
 */
@SuppressWarnings("javasecurity:S5131")
@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final RestClient restClient;
    private final TargetConfigProps targetConfigProps;

    public PartnerController(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory, TargetConfigProps targetConfigProps) {
        // By creating the RestClient builder with the JeapOAuth2RestClientBuilderFactory all
        // RestClient instances built with this builder will add an OAuth2 access token
        // to the exchanges based on the provided Spring client registration as defined in the application*.yml.
        this.restClient = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId("jme-security-oauth2-client-service").build();
        this.targetConfigProps = targetConfigProps;
    }

    @GetMapping
    public String listPartners() {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor).
        String response = restClient.get().
                uri(targetConfigProps.getResource() + "/api/partners").
                retrieve().
                body(String.class);
        return String.format("Partner list: %s", response);
    }

    @GetMapping("/{partner}")
    public String getPartner(@PathVariable("partner") String partner) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor)
        String response = restClient.get().
                uri(targetConfigProps.getResource() + "/api/partners/{partner}", partner).
                retrieve().
                body(String.class);
        return String.format("Partner '%s' data: %s", sanitize(partner), response);
    }

    @GetMapping("/{partner}/name")
    public String getPartnerName(@PathVariable("partner") String partner) {
        // This RestClient exchange will include a bearer OAuth2 access token (see creation of the client in the constructor)
        String response = restClient.get().
                uri(targetConfigProps.getResource() + "/api/partners/{partner}/name", partner).
                retrieve().
                body(String.class);
        return String.format("Partner '%s' name: %s", sanitize(partner), response);
    }

}
