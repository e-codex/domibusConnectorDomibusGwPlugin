package eu.ecodex.domibus.gw.init;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.JarFileArchive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;


public class DomibusDbInitListener implements LifecycleListener {

    private static Logger LOGGER = Logger.getLogger(DomibusDbInitListener.class.getName());

    public static final String DOMIBUS_DATABASE_URL = "domibus.datasource.url";

    public static final String JAR_RESOURCE_LOCATION = "/gw-liquibase-db-init.jar";

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        Lifecycle lifecycle = event.getLifecycle();
        LifecycleState state = lifecycle.getState();

        String tempDir = System.getProperty("java.io.tmpdir");

        if (Lifecycle.BEFORE_START_EVENT.equals(event.getType())) {
            LOGGER.info("Running DB init");

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader(); //save classloader

            try (InputStream is = getClass().getResourceAsStream(JAR_RESOURCE_LOCATION)) {
                if (is == null) {
                    throw new IllegalArgumentException("Input stream of " + JAR_RESOURCE_LOCATION + " cannot be null!");
                }

                Path tempFile = Paths.get(tempDir)
                        .resolve("gw-liquibase-db-init.jar");

                try (FileOutputStream fos = new FileOutputStream(tempFile.toFile());) {
                    IOUtils.copy(is, fos);
                } catch (IOException ioe) {
                    LOGGER.severe("Failed to write to " + tempFile + " " + ioe.getMessage());
                    ioe.printStackTrace();
                    System.exit(1);
                }

                Archive a = new JarFileArchive(tempFile.toFile());
                MyJarLauncher myJarLauncher = new MyJarLauncher(a);
                myJarLauncher.launch(new String[]{"--spring.config.location=${catalina.home}/conf/domibus/domibus.properties"});

                gracefullyDelete(tempFile);
            } catch (Exception e) {
                LOGGER.severe(e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }

            Thread.currentThread().setContextClassLoader(contextClassLoader); //restore classloader

        }

    }

    private static void gracefullyDelete(Path tempFile) {
        try {
            Files.delete(tempFile);
        } catch (IOException ioe) {
            //ignore..
            LOGGER.warning("Failed to delete: " + ioe.getMessage());
        }
    }

    private static class MyJarLauncher extends JarLauncher {
        public MyJarLauncher(Archive archive) {
            super(archive);
        }

        @Override
        protected void launch(String[] args) throws Exception {
            super.launch(args);
        }
    }

}
