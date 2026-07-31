package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.authentication.ServletSimpleAuthorization;
import ch.admin.bit.jeap.security.test.resource.ServletSimpleAuthorizationMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

/**
 * This test class shows an example of unit testing a class that relies on a ServletSimpleAuthorization instance.
 * Such cases occur when programmatic authorization tests or access to the user/system data of the authentication are needed.
 * To check the functionality of such classes the ServletSimpleAuthorization instance has to be mocked. This can be done
 * using e.g. Mockito or using the convenient ServletSimpleAuthorizationMock class supplied by the
 * jeap-spring-boot-security-test-starter.
 */
class PartnerResourceTest {

	@Test
    void testGetPartnerNameByExternalId_whenPartnerReadRole_thenSuccess() {
        ServletSimpleAuthorization jeapAuthorization = ServletSimpleAuthorizationMock.builder()
                .userRole("partner_read")
                .build();
        PartnerResource partnerResource = new PartnerResource(jeapAuthorization);

        String name = partnerResource.getPartnerNameByExternalRef("eins");

        Assertions.assertEquals("Partner 1", name);
    }

    @Test
    void testGetPartnerNameByExternalId_whenMissingRole_thenForbidden() {
        ServletSimpleAuthorization jeapAuthorization = ServletSimpleAuthorizationMock.builder().build();
        PartnerResource partnerResource = new PartnerResource(jeapAuthorization);

        Assertions.assertThrows(AccessDeniedException.class, () -> partnerResource.getPartnerNameByExternalRef("eins"));
    }

	// Additional tests....

}
