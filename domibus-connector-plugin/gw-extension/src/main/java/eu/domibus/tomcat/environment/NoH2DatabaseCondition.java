package eu.domibus.tomcat.environment;

//import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition to check that Domibus is not running with H2 database. Used to exclude the Tomcat datasource Spring instantiation to avoid collision with the test data source.
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
public class NoH2DatabaseCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Environment environment = conditionContext.getEnvironment();
        String currentDatabase = environment.getProperty("domibus.datasource.driverClassName");
        String h2Allowed = environment.getProperty("domibus.datasource.allowH2Driver");
        return !StringUtils.contains(currentDatabase, "org.h2.Driver") || "true".equals(h2Allowed);
    }
}
