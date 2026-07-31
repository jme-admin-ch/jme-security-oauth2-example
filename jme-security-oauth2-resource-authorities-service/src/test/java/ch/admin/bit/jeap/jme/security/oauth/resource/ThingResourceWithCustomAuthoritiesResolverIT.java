package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.token.AuthoritiesResolver;
import ch.admin.bit.jeap.security.resource.token.JeapAuthenticationContext;
import ch.admin.bit.jeap.security.test.jws.JwsBuilder;
import ch.admin.bit.jeap.security.test.jws.JwsBuilderFactory;
import ch.admin.bit.jeap.security.test.resource.configuration.JeapOAuth2IntegrationTestResourceConfiguration;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

/**
 * This test class gives an example for an integration test that checks the REST endpoint of a Spring Boot application
 * that is protected with the jeap spring boot security starter with a modified Authorities resolver.
 * <p>
 * Typically, you might want to test the production resolver code instead of a mocked one. If you require different
 * resolver patterns for your tests, this test shows you how.
 * <p>
 * Please refer to the module `jme-security-oauth2-resource-service` for the general OAuth2 configuration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {"server.port=8888",
                // Override the external authorization server JWKS configuration, use the test support JWKS endpoint instead.
                "jeap.security.oauth2.resourceserver.authorization-server.issuer=" + JwsBuilder.DEFAULT_ISSUER,
                // For your own implementation, don't forget to adapt the service context (jme-security-oauth2-resource-authorities-service) to your own service context.
                "jeap.security.oauth2.resourceserver.authorization-server.jwk-set-uri=http://localhost:${server.port}/jme-security-oauth2-resource-authorities-service/.well-known/jwks.json"})
@Import({JeapOAuth2IntegrationTestResourceConfiguration.class, // Enable test support for OAuth2 integration testing
        ThingResourceWithCustomAuthoritiesResolverIT.IntegrationTestConfiguration.class})
@ActiveProfiles({"local"})
class ThingResourceWithCustomAuthoritiesResolverIT {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private JwsBuilderFactory jwsBuilderFactory;

    private static final String THING_ID_PARAM_NAME = "id";
    private static final String THINGS_BASE_URL = "/jme-security-oauth2-resource-authorities-service/api/things";
    private static final String THINGS_GET_ONE_URL_TEMPLATE = THINGS_BASE_URL + "/{" + THING_ID_PARAM_NAME + "}";

    private static final String SUBJECT = "69368608-D736-43C8-5F76-55B7BF168299";
    private static final JeapAuthenticationContext CONTEXT = JeapAuthenticationContext.SYS;

    private final RequestSpecification thingsBaseUrlSpec;
    private final RequestSpecification thingsGetOneUrlTemplateSec;

    @Autowired
    ThingResourceWithCustomAuthoritiesResolverIT(@Value("${server.port}") int serverPort) {
        thingsBaseUrlSpec = new RequestSpecBuilder().setBasePath(THINGS_BASE_URL).setPort(serverPort).build();
        thingsGetOneUrlTemplateSec = new RequestSpecBuilder().setBasePath(THINGS_GET_ONE_URL_TEMPLATE).setPort(serverPort).build();
    }

    @TestConfiguration
    public static class IntegrationTestConfiguration {

        @Bean
        // Typically, you want to test the production resolver code instead of a mocked one. If you require different
        // resolver patterns for your tests, you can inject a different resolver using the `@Primary` annotation.
        @Primary
        public AuthoritiesResolver testAuthoritiesResolver() {
            return (userRoles, businessPartnerRoles) -> {
                List<String> grantedAuthorities = new ArrayList<>(List.of("things:read"));
                if (userRoles.contains("superadmin")) {
                    grantedAuthorities.add("things:detail");
                }
                return grantedAuthorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
            };
        }
    }


    // @formatter:off
    @ParameterizedTest
    @ValueSource(strings = {"user", "admin", "superadmin"})
    void testListThings(String role) {
        String authToken = createAuthTokenForUserRoles(role);
        given().
                spec(thingsBaseUrlSpec).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(HttpStatus.OK.value());
    }

    @ParameterizedTest
    @CsvSource({"user,403", "admin,403", "superadmin,200"})
    void testGetThingDetails(String role, Integer expectedResponseCode) {
        String authToken = createAuthTokenForUserRoles(role);
        given().
                spec(thingsGetOneUrlTemplateSec).
                pathParam(THING_ID_PARAM_NAME, 8).
                auth().oauth2(authToken).
        when().
                get().
        then().
                statusCode(expectedResponseCode);
    }
    // @formatter:on

    private String createAuthTokenForUserRoles(String... roles) {
        return jwsBuilderFactory.createValidForFixedLongPeriodBuilder(SUBJECT, CONTEXT).
                withClaim("role", roles).
                build().serialize();
    }
}
