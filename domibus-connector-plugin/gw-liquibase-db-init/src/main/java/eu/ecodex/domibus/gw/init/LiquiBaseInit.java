package eu.ecodex.domibus.gw.init;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
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

    @Bean
    @Primary
    DataSource domibusDatasource(Environment env) {
        return DataSourceBuilder.create()
                .url(env.getRequiredProperty("domibus.datasource.url"))
                .username(env.getRequiredProperty("domibus.datasource.user"))
                .password(env.getRequiredProperty("domibus.datasource.password"))
                .driverClassName(env.getRequiredProperty("domibus.datasource.driverClassName"))
                .build();
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
