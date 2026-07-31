package ch.admin.bit.jeap.jme.security.oauth.client.smoketest;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

@Slf4j
@EnabledIfSystemProperty(named = AfterDeploymentSimpleRolesSmokeTestIT.DEPLOY_STAGE_PROPERTY_NAME, matches = "d")
class AfterDeploymentSimpleRolesSmokeTestIT {

    static final String DEPLOY_STAGE_PROPERTY_NAME = "deployStage";

    private static final String CLIENT_SERVICE_BASE_PATH = "/jme-security-oauth2-client-service/api"; // webmvc service chain

    private static final String[] THING_IDS = {
            "1", // uses declarative authorization checks in the resource
            "9" // uses programmatic authorization checks in the resource
    };
    private static final String[] PARTNER_IDS = {
            "11111", // unconditional reuse of the token in the request by the client resource service
            "99999" // reuse of the token in the request by the client resource service for the 'token from request preferred' case
    };

    private RequestSpecification request;

    @Test
    void testGetAllThings() {
        log.info("testGetAllThings(clientPath:{})", CLIENT_SERVICE_BASE_PATH);
        given().
                spec(request).
                basePath(CLIENT_SERVICE_BASE_PATH).
        when().
                get("/things").
        then().
                statusCode(HttpStatus.OK.value()).
                body(startsWith("Got things:"), containsString("Thing1"), containsString("Thing9"));
    }

    @ParameterizedTest
    @MethodSource("clientServiceBasePathAndThingId")
    void testGetThings(String clientPath, String thingId) {
        log.info("testGetThings(clientPath:{}, thingId:{})", clientPath, thingId);
        given().
                spec(request).
                basePath(clientPath).
        when().
                get("/things/{thingId}", thingId).
        then().
                statusCode(HttpStatus.OK.value()).
                body(startsWith("Got thing with id '" + thingId + "'"));
    }

    @Test
    void testGetThingsForPartner() {
        log.info("testGetThingsForPartner(clientPath:{})", CLIENT_SERVICE_BASE_PATH);
        given().
                spec(request).
                basePath(CLIENT_SERVICE_BASE_PATH).
        when().
               get("/partners/11111/things").
        then().
               statusCode(HttpStatus.OK.value()).
               body(startsWith("Got things for partner with id 11111"));
    }

    @Test
    void testGetAllPartners() {
        log.info("testGetAllPartners(clientPath:{})", CLIENT_SERVICE_BASE_PATH);
        given().
                spec(request).
                basePath(CLIENT_SERVICE_BASE_PATH).
        when().
                get("/partners").
        then()
                .statusCode(HttpStatus.OK.value())
                .body(startsWith("Partner list:"), containsString("Partner 1"));
    }

    @ParameterizedTest
    @MethodSource("clientServiceBasePathAndPartnerId")
    void testGetPartners(String clientPath, String partnerId) {
        log.info("testGetPartners(clientPath:{}, partnerId:{})", clientPath, partnerId);
        given().
                spec(request).
                basePath(clientPath).
        when().
                get("/partners/{partnerId}", partnerId).
        then()
                .statusCode(HttpStatus.OK.value())
                .body(startsWith("Partner '" + partnerId + "' data"));
    }

    @Test
    void testGetPartnerNameByExternalReference() {
        log.info("testGetPartnerNameByExternalReference(clientPath:{})", CLIENT_SERVICE_BASE_PATH);
        given().
                spec(request).
                basePath(CLIENT_SERVICE_BASE_PATH).
        when().
                get("/partners/eins/name").
        then()
                .statusCode(HttpStatus.OK.value())
                .body(startsWith("Partner 'eins' name: Partner 1"));
    }

    @Test
    void testGetInfo() {
        log.info("testGetInfo(clientPath:{})", CLIENT_SERVICE_BASE_PATH);
        given().
                spec(request).
                basePath(CLIENT_SERVICE_BASE_PATH).
        when().
                get("/info").
        then()
                .statusCode(HttpStatus.OK.value())
                .body(startsWith("Info : Some info"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetCustomizedCurrentUserInfo() {
        String clientPath = "/jme-security-oauth2-client-service/api";
        log.info("testGetCurrentUserInfo(clientPath:{})", clientPath);
        Map<String, Object> response = given().
                spec(request).
                basePath(clientPath).
                when().
                get("/current-user").
                then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response()
                .as(Map.class);

        assertThat(response)
                .containsEntry("myCustomValue", "fooBar")
                .containsKey("subject");

        assertThat((List<String>) response.get("userRoles"))
                .contains("partner_read")
                .contains("thing_read");
    }


    @BeforeEach
    void setUp() {
        String deployStage = System.getProperty(DEPLOY_STAGE_PROPERTY_NAME, "d");
        log.info("deployStage is {}", deployStage);
        String baseUri = "https://bit-jme-%s.apps.p-szb-ros-shrd-npr-01.cloud.admin.ch".formatted(deployStage);
        RestAssured.useRelaxedHTTPSValidation();

        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri(baseUri);
        request = builder.build();
    }

    private static Stream<Arguments> clientServiceBasePathAndThingId() {
        return Arrays.stream(THING_IDS).map(thingId -> Arguments.of(CLIENT_SERVICE_BASE_PATH, thingId));
    }

    private static Stream<Arguments> clientServiceBasePathAndPartnerId() {
        return Arrays.stream(PARTNER_IDS).map(thingId -> Arguments.of(CLIENT_SERVICE_BASE_PATH, thingId));
    }

}
