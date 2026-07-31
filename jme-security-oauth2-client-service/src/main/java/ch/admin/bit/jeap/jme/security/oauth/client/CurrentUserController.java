package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * This particular endpoint is designed to emulate access to the current-user endpoint with the use of a token, subsequently returning a JSON response.
 * It should be noted that this endpoint is exclusively utilized for testing purposes. Typically, the current-user endpoint is exclusively used by the frontend.
 */
@RestController
@RequestMapping("/api/current-user")
@Slf4j
public class CurrentUserController {

    private final RestClient restClient;
    private final TargetConfigProps targetConfigProps;

    public CurrentUserController(JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory, TargetConfigProps targetConfigProps) {
        this.restClient = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId("jme-security-oauth2-client-service").build();
        this.targetConfigProps = targetConfigProps;
    }

    @GetMapping(produces = "application/json")
    public String currentUserInfo() {
        return restClient.get().
                uri(targetConfigProps.getResource() + "/api/current-user").
                retrieve().
                body(String.class);
    }
}
