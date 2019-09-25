package eu.domibus.connector.plugin.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class PluginInitializerTest {


    @Test
    public void testPluginInit() {
        PluginInitializer pluginInitializer = new PluginInitializer();
        pluginInitializer.init();

        assertThat(pluginInitializer.getDomibusConnectorWebservice()).isNotNull();

    }

}