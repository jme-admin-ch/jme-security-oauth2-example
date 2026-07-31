package ch.admin.bit.jeap.jme.security.oauth.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("target")
public class TargetConfigProps {
    private String resource;
}
