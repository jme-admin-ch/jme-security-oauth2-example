package ch.admin.bit.jeap.jme.security.oauth.client.smoketest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
@EnabledIfSystemProperty(named = AfterDeploymentAuthoritiesSmokeTestIT.DEPLOY_STAGE_PROPERTY_NAME, matches = "d")
class AfterDeploymentAuthoritiesSmokeTestIT {

    static final String DEPLOY_STAGE_PROPERTY_NAME = "deployStage";
    private static final String CLIENT_SERVICE_BASE_PATH = "/jme-security-oauth2-client-authorities-service/api";

    private RequestSpecification request;

    @Test
    void testGetAllThings() {
        given().
                spec(request).
        when().
                get("/things").
        then().
                statusCode(HttpStatus.OK.value()).
                body(startsWith("Got things:"), containsString("Thing1"), containsString("Thing9"));
    }

    @Test
    void testGetThings() {
        final String thingId = "1";
        given().
                spec(request).
        when().
                get("/things/{thingId}", thingId).
        then().
                statusCode(HttpStatus.OK.value()).
                body(startsWith("Got thing with id '" + thingId + "'"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetCustomizedCurrentUserInfo() {
        Map<String, Object> response = given().
                spec(request).
                when().
                get("/current-user").
                then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response()
                .as(Map.class);

        assertThat(response)
                .containsKey("subject");

        assertThat((List<String>) response.get("userRoles"))
                .contains("user")
                .contains("admin")
                .contains("superadmin");
    }

    @Test
    void testGetInfo() {
        given().
                spec(request).
        when().
                get("/info").
        then()
                .statusCode(HttpStatus.OK.value())
                .body(startsWith("Info : Some info"));
    }

    @BeforeEach
    void setUp() {
        String deployStage = System.getProperty(DEPLOY_STAGE_PROPERTY_NAME, "d");
        log.info("deployStage is {}", deployStage);
        String baseUri = "https://bit-jme-%s.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch".formatted(deployStage);
        RestAssured.useRelaxedHTTPSValidation();

        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(baseUri);
        builder.setBasePath(CLIENT_SERVICE_BASE_PATH);
        request = builder.build();
    }

}
