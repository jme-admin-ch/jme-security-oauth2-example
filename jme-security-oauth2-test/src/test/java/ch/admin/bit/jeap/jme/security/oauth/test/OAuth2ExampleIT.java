package ch.admin.bit.jeap.jme.security.oauth.test;

import ch.admin.bit.jeap.jme.test.BootServiceSpringIntegrationTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;

class OAuth2ExampleIT extends BootServiceSpringIntegrationTestBase {

    private static final String AUTH_BASE_URL = "http://localhost:8081/jme-security-oauth2-auth-scs";
    private static final String RESOURCE_BASE_URL = "http://localhost:8070/jme-security-oauth2-resource-service";
    private static final String AUTHORITIES_RESOURCE_BASE_URL =
            "http://localhost:8072/jme-security-oauth2-resource-authorities-service";
    private static final String CLIENT_BASE_URL = "http://localhost:8090/jme-security-oauth2-client-service";
    private static final String AUTHORITIES_CLIENT_BASE_URL =
            "http://localhost:8092/jme-security-oauth2-client-authorities-service";

    @BeforeAll
    static void startServices() throws Exception {
        ensurePortsAvailable(8081, 8070, 8072, 8090, 8092);
        startService("jme-security-oauth2-auth-scs", AUTH_BASE_URL);
        startService("jme-security-oauth2-resource-service", RESOURCE_BASE_URL);
        startService("jme-security-oauth2-resource-authorities-service", AUTHORITIES_RESOURCE_BASE_URL);
        startService("jme-security-oauth2-client-service", CLIENT_BASE_URL);
        startService("jme-security-oauth2-client-authorities-service", AUTHORITIES_CLIENT_BASE_URL);
    }

    private static void ensurePortsAvailable(int... ports) throws IOException {
        for (int port : ports) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress("localhost", port));
            } catch (IOException e) {
                throw new IOException("Port " + port + " is required by the OAuth2 integration test but is in use", e);
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void simpleRolesClientCallsResourceChain() {
        given()
                .baseUri(CLIENT_BASE_URL)
                .when()
                .get("/api/things")
                .then()
                .statusCode(200)
                .body(startsWith("Got things:"), containsString("Thing1"), containsString("Thing9"));

        given()
                .baseUri(CLIENT_BASE_URL)
                .when()
                .get("/api/info")
                .then()
                .statusCode(200)
                .body(startsWith("Info : Some info"));

        Map<String, Object> currentUser = given()
                .baseUri(CLIENT_BASE_URL)
                .when()
                .get("/api/current-user")
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class);

        assertThat(currentUser).containsEntry("myCustomValue", "fooBar").containsKey("subject");
        assertThat((List<String>) currentUser.get("userRoles")).contains("partner_read", "thing_read");
    }

    @Test
    @SuppressWarnings("unchecked")
    void customAuthoritiesClientCallsResourceChain() {
        given()
                .baseUri(AUTHORITIES_CLIENT_BASE_URL)
                .when()
                .get("/api/things/1")
                .then()
                .statusCode(200)
                .body(startsWith("Got thing with id '1'"));

        given()
                .baseUri(AUTHORITIES_CLIENT_BASE_URL)
                .when()
                .get("/api/info")
                .then()
                .statusCode(200)
                .body(startsWith("Info : Some info"));

        Map<String, Object> currentUser = given()
                .baseUri(AUTHORITIES_CLIENT_BASE_URL)
                .when()
                .get("/api/current-user")
                .then()
                .statusCode(200)
                .extract()
                .as(Map.class);

        assertThat(currentUser).containsKey("subject");
        assertThat((List<String>) currentUser.get("userRoles")).contains("user", "admin", "superadmin");
    }
}
