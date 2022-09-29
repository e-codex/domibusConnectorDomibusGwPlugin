package eu.ecodex.domibus.gw.init;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@SpringBootApplication
@PropertySource("classpath:/default.properties")
public class LiquiBaseInit {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .properties("spring.liquibase.change-log=classpath:initdb.xml", "spring.sql.init.mode=embedded")

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
            liquibase.change.DatabaseChange c;
        }
    }

}
