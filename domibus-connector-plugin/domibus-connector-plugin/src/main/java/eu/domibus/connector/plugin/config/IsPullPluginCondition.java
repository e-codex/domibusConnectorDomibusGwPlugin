package eu.domibus.connector.plugin.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IsPullPluginCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "PULL".equalsIgnoreCase(context.getEnvironment().getProperty(DCPluginConfiguration.PLUGIN_DELIVERY_MODE));
    }
}
