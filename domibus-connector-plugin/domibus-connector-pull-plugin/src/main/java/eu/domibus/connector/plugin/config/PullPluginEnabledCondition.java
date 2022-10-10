package eu.domibus.connector.plugin.config;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;



import static eu.domibus.connector.plugin.config.property.DCPullPluginPropertyManager.DC_PULL_PLUGIN_ENABLED_PROPERTY_NAME;

public class PullPluginEnabledCondition implements Condition {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(PullPluginEnabledCondition.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Environment environment = conditionContext.getEnvironment();
        String dcPullPluginEnabled = environment.getProperty(DC_PULL_PLUGIN_ENABLED_PROPERTY_NAME);
        boolean isEnabled = "true".equalsIgnoreCase(dcPullPluginEnabled);
        LOGGER.debug("PullPluginEnabledCondition is [{}]", isEnabled);
        return isEnabled;
    }

}
