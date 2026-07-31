package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.claimsetconverter.EiamClaimSetConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthoritiesServiceConfiguration {

    @Bean("eiamClaimSetConverter")
    EiamClaimSetConverter eiamClaimSetConverter() {
        return new EiamClaimSetConverter();
    }

}
