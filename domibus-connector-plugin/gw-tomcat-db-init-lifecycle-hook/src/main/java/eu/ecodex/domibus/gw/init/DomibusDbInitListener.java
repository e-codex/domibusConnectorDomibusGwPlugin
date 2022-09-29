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
import java.util.logging.Logger;


public class DomibusDbInitListener implements LifecycleListener {

    private static Logger LOGGER = Logger.getLogger(DomibusDbInitListener.class.getName());


    public static final String DOMIBUS_DATABASE_URL = "domibus.datasource.url";

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        Lifecycle lifecycle = event.getLifecycle();
        LifecycleState state = lifecycle.getState();

        if (LifecycleState.STARTING_PREP == state) {
            LOGGER.info("Running DB init");

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader(); //save classloader

            try (InputStream is = getClass().getResourceAsStream("/gw-liquibase-db-init.jar")) {


                File tempFile = File.createTempFile("gw-liquibase-db-init", ".jar");
                try (FileOutputStream fos = new FileOutputStream(tempFile);) {
                    IOUtils.copy(is, fos);
                }

                Archive a = new JarFileArchive(tempFile);
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

    private static void gracefullyDelete(File tempFile) {
        try {
            Files.delete(tempFile.toPath());
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
