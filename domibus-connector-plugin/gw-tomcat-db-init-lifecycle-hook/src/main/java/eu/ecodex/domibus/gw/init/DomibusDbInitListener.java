package eu.ecodex.domibus.gw.init;

import org.apache.catalina.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.JarLauncher;
import org.springframework.boot.loader.archive.JarFileArchive;


public class DomibusDbInitListener implements LifecycleListener {

    private static Logger LOG = Logger.getLogger(DomibusDbInitListener.class.getName());


    public static final String DOMIBUS_DATABASE_URL = "domibus.datasource.url";

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        String type = event.getType();
        Lifecycle lifecycle = event.getLifecycle();
        LifecycleState state = lifecycle.getState();

        Context context = (Context) event.getLifecycle();

//        System.out.printf("\n\n\nLIFECYCLE EVENT + " + state + "\n\n");

//        LOG.info("Lifecycle Event: " + state);
        if (LifecycleState.STARTING_PREP == state) {
            LOG.info("Running DB init");
//            LiquiBaseInit.main();
//            JarLauncher j;

            Semaphore s = new Semaphore(0);
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader(); //save classloader

//            Thread t = new Thread( () -> {
                try (InputStream is = getClass().getResourceAsStream("/gw-liquibase-db-init.jar")){


                    File tempFile = File.createTempFile("gw-liquibase-db-init", ".jar");
                    try (FileOutputStream fos = new FileOutputStream(tempFile);) {
                        IOUtils.copy(is, fos);
                    }

                    Archive a = new JarFileArchive(tempFile);
                    MyJarLauncher myJarLauncher = new MyJarLauncher(a);
                    myJarLauncher.launch(new String[]{"--spring.config.location=${catalina.home}/conf/domibus/domibus.properties"});

                } catch (Exception e) {
                    LOG.severe(e.getMessage());
                    e.printStackTrace();
                    System.exit(1);
                }

                Thread.currentThread().setContextClassLoader(contextClassLoader); //restore classloader

//                s.release();
//            });
//
//            t.start(); // run in seperate thread so spring does not interfer with class loader of current thread
//            try {
//                s.acquire(); // wait for t to finish
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }


        }

    }

    public static class MyJarLauncher extends JarLauncher {
        public MyJarLauncher(Archive archive) {
            super(archive);
        }

        protected void launch(String[] args) throws Exception {
            super.launch(args);
        }
    }

}
