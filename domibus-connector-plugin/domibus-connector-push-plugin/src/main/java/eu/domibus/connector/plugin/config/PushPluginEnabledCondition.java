package eu.domibus.connector.plugin.config;

import eu.domibus.connector.plugin.config.property.DCPushPluginPropertyManager;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;


public class PushPluginEnabledCondition implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(PushPluginEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Environment environment = conditionContext.getEnvironment();
        String dcPluginEnabled = environment.getProperty(DCPushPluginPropertyManager.DC_PUSH_PLUGIN_ENABLED_PROPERTY_NAME);
        boolean isEnabled = "true".equalsIgnoreCase(dcPluginEnabled);
        LOGGER.info("PushPluginEnabledCondition is [{}]", isEnabled);
        return isEnabled;
    }

}
