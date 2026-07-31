package ch.admin.bit.jeap.jme.security.oauth.client;

import ch.admin.bit.jeap.security.restclient.JeapOAuth2RestClientBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/info")
@Slf4j
public class InfoController {

    private final RestClient restClient;

    public InfoController(TargetConfigProps targetConfigProps, JeapOAuth2RestClientBuilderFactory jeapOAuth2RestClientBuilderFactory) {
        this.restClient = jeapOAuth2RestClientBuilderFactory.createForClientRegistryId("jme-security-oauth2-client-authorities-service")
                .baseUrl(targetConfigProps.getResource()).build();
    }

    @GetMapping
    public String getInfo() {
        String response = restClient.get().
                uri("/api/info").
                retrieve().
                body(String.class);
        return String.format("Info : %s", response);
    }
}
