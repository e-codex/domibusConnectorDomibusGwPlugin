package eu.ecodex.domibus.gw.init;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@SpringBootApplication
@ImportAutoConfiguration(DataSourceAutoConfiguration.class)
public class LiquiBaseInit {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = new SpringApplicationBuilder()
                .sources(LiquiBaseInit.class)
                .run(args);
    }

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "domibus.datasource")
    DataSourceProperties domibusDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    DataSource domibusDatasource(DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    @Component
    public static class CommandLineRunnerLiqui implements CommandLineRunner {

        public CommandLineRunnerLiqui(DataSource ds) {
            this.ds = ds;
        }

        private final DataSource ds;

        @Override
        public void run(String... args) throws Exception {

        }
    }

}
