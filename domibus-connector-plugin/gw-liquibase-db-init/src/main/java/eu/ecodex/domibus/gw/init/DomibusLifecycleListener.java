package eu.ecodex.domibus.gw.init;

import org.apache.catalina.*;

import java.util.logging.Logger;


public class DomibusLifecycleListener implements LifecycleListener {

    private static Logger LOG = Logger.getLogger(DomibusLifecycleListener.class.getName());


    public static final String DOMIBUS_DATABASE_URL = "domibus.datasource.url";

    @Override
    public void lifecycleEvent(LifecycleEvent event) {

        String type = event.getType();
        Lifecycle lifecycle = event.getLifecycle();
        LifecycleState state = lifecycle.getState();

        Context context = (Context) event.getLifecycle();


        if (LifecycleState.INITIALIZING == state) {
            //TODO: start spring APP
        }

    }

}
