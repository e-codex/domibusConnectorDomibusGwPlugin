package eu.ecodex.domibus.gw.init;


import org.apache.catalina.Context;

import org.apache.catalina.Loader;
import org.apache.catalina.loader.WebappLoader;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.apache.catalina.startup.Tomcat;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@SpringBootApplication
@ImportAutoConfiguration({DataSourceAutoConfiguration.class})
public class DomibusGatewayApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomibusGatewayApplication.class);

    public static void main(String[] args) {
        System.getProperties().setProperty("domibus.config.location", "conf/domibus/");
        System.getProperties().setProperty("spring.config.location", "conf/domibus/domibus.properties");

        ConfigurableApplicationContext run = new SpringApplicationBuilder()
                .sources(DomibusGatewayApplication.class)
                .web(WebApplicationType.SERVLET)
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


    @Bean
    public TomcatServletWebServerFactory domibusServletContainerFactory() {
        return new TomcatServletWebServerFactory() {

            @Override
            protected TomcatWebServer getTomcatWebServer(Tomcat tomcat) {

                // webapps directory does not exist by default, needs to be created
                new File(tomcat.getServer().getCatalinaBase(), "webapps").mkdirs();

                Loader loader;
                try {
                    Context context = tomcat.addWebapp("/domibus", new ClassPathResource("domibus-MSH-tomcat-5.0.war").getFile().toString());

//                    tomcat.getEngine().getParentClassLoader()
//                    context.getLoader().getClassLoader();
//                    context.getLoader().setDelegate(true);
                    loader = context.getLoader();



                } catch (Exception ex) {
                    throw new IllegalStateException("Failed to add webapp", ex);
                }


                return super.getTomcatWebServer(tomcat);
            }

        };
    }
}
