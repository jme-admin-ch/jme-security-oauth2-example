package ch.admin.bit.jeap.jme.security.oauth.resource;

import ch.admin.bit.jeap.security.resource.configuration.JeapMethodSecurityExpressionHandlerCustomizer;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.stereotype.Component;

/**
 * Only needed for special cases when you have to customize the method security expression handler instantiated by
 * the jEAP security starter. Use e.g. to register a custom permission evaluator.
 */
@Component
public class MethodSecurityExpressionHandlerCustomizer implements JeapMethodSecurityExpressionHandlerCustomizer {
    @Override
    public MethodSecurityExpressionHandler customize(DefaultMethodSecurityExpressionHandler expressionHandler) {
        expressionHandler.setPermissionEvaluator(new ThingPermissionEvaluator());
        return expressionHandler;
    }
}
