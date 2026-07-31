package ch.admin.bit.jeap.jme.security.oauth.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * This endpoint makes the basic auth protected endpoints of the jme-security-oauth2-client-resource's 'info' resource publicly
 * available by forwarding corresponding requests to the resource's endpoints equipping the requests with appropriate
 * username password credentials.
 */
@RestController
@RequestMapping("/api/info")
@Slf4j
public class InfoController {

    private final RestClient restClient;
    private final TargetConfigProps targetConfigProps;

    public InfoController(RestClient.Builder restClientBuilder, TargetConfigProps targetConfigProps) {
        // By using the 'standard' RestClient builder bean provided by Spring Boot (instead of a builder created with
        // the JeapOAuth2RestClientBuilderFactory) a 'standard' RestClient instance will be built, i.e. one that
        // will not include an OAuth2 access token in its exchanges.
        this.restClient = restClientBuilder.clone().
                defaultHeaders(headers -> headers.setBasicAuth("user", "secret")).
                build();
        this.targetConfigProps = targetConfigProps;
    }

    @GetMapping
    public String getInfo(@RequestParam("target") Optional<String> target) {
        // This RestClient exchange will include a basic authorization header, but no OAuth2 token (see creation of the client in the constructor).
        String response = restClient.get().
                uri(targetConfigProps.getResource() + "/api/info").
                retrieve().
                body(String.class);
        return String.format("Info : %s", response);
    }

}
